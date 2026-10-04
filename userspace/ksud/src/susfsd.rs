//! [ShizuSU 补丁4 · 4.1] SuSFS 运行控制通道（查询/上报桥）。
//!
//! 照搬自 KernelSU-Next `userspace/ksud/src/susfsd.rs`（upstreams/next @ 3b1e46b，115 行）的
//! **对外行为与公共 API 形状**：`show_version() / show_variant() / show_features(check_only)`
//! 三个 `pub fn -> anyhow::Result<()>`，经 `reboot(2)` 通道向（外部补丁版）SuSFS 内核查询。
//!
//! ## 与基线的关系（最小 diff，不重复造通道）
//! 基线 SukiSU ksud **本就有**该 reboot 通道，只是组织在 `susfs/` 模块树而非独立 `susfsd.rs`：
//!  - 魔数/命令常量：`susfs/abi/consts.rs`（KSU_INSTALL_MAGIC1=0xDEADBEEF :8、
//!    SUSFS_MAGIC=0xFAFAFAFA :11、CMD_SUSFS_SHOW_VERSION=0x0005_55E1 :38、
//!    SHOW_ENABLED_FEATURES=:39、SHOW_VARIANT=:40、ERR_CMD_NOT_SUPPORTED=126 :15）——与 Next
//!    susfsd.rs:6-17 **逐值一致**；
//!  - `reboot(2)` 胶水：`susfs/abi/syscall.rs::send()`（:33 `syscall(SYS_reboot, magic1, magic2,
//!    cmd, payload)`）——与 Next susfsd.rs:44/65/86 同一调用形；
//!  - `#[repr(C)]` 结构体：`susfs/abi/types.rs`（SusfsVersion/SusfsFeatures/SusfsVariant，
//!    与 Next susfsd.rs:20-35 同布局、仅字段名不同）。
//!
//! 故本文件**不重复** syscall 胶水与结构体，而是把 Next 的三个查询函数 + `check_unsupported`
//! 错误语义作为 façade 搭在基线上方（清单 4.1② 定案二选一中的「并入现有 susfs 模块」）。
//!
//! ## 端到端生效 = 待实测
//! 内核侧消费端（0xFAFAFAFA + 0x555e1/2/3）**不在本仓库树内**（notes/next.md §9：Next/基线
//! kernel/ 全树 grep SUSFS=0 命中），由外部 susfs4ksu 内核补丁提供。未打补丁的内核上查询返回
//! err=126，本模块按 Next 行为报 "SUSFS operation not supported, please enable it in kernel"。

use anyhow::{Result, anyhow};

use crate::susfs::abi::consts::{
    CMD_SUSFS_SHOW_ENABLED_FEATURES, CMD_SUSFS_SHOW_VARIANT, CMD_SUSFS_SHOW_VERSION,
    ERR_CMD_NOT_SUPPORTED,
};
use crate::susfs::abi::{send, SusfsFeatures, SusfsVariant, SusfsVersion};
use crate::susfs::util::cstr_buf_to_string;

/// 照搬 Next susfsd.rs:37-56。经 reboot 通道查询并打印 SuSFS 版本字符串。
pub fn show_version() -> Result<()> {
    let mut cmd = SusfsVersion {
        susfs_version: [0; 16],
        err: ERR_CMD_NOT_SUPPORTED,
    };
    send(CMD_SUSFS_SHOW_VERSION, &mut cmd, "show_version")?;

    check_unsupported(cmd.err, CMD_SUSFS_SHOW_VERSION)?;

    if cmd.err == 0 {
        println!("{}", cstr_buf_to_string(&cmd.susfs_version));
        Ok(())
    } else {
        Err(anyhow!("Invalid (Error: {})", cmd.err))
    }
}

/// 照搬 Next susfsd.rs:58-77。打印 SuSFS variant（如 "gki"/"non-gki"）。
pub fn show_variant() -> Result<()> {
    let mut cmd = SusfsVariant {
        susfs_variant: [0; 16],
        err: ERR_CMD_NOT_SUPPORTED,
    };
    send(CMD_SUSFS_SHOW_VARIANT, &mut cmd, "show_variant")?;

    check_unsupported(cmd.err, CMD_SUSFS_SHOW_VARIANT)?;

    if cmd.err == 0 {
        println!("{}", cstr_buf_to_string(&cmd.susfs_variant));
        Ok(())
    } else {
        Err(anyhow!("Invalid (Error: {})", cmd.err))
    }
}

/// 照搬 Next susfsd.rs:79-107。`check_only=true` 时只打印 "Supported"/"Unsupported"
/// （对齐 Next `SusfsAction::Support`）；否则打印逗号分隔的已启用特性串。
pub fn show_features(check_only: bool) -> Result<()> {
    let mut cmd = SusfsFeatures {
        enabled_features: [0; 8192],
        err: ERR_CMD_NOT_SUPPORTED,
    };
    send(
        CMD_SUSFS_SHOW_ENABLED_FEATURES,
        &mut cmd,
        "show_enabled_features",
    )?;

    check_unsupported(cmd.err, CMD_SUSFS_SHOW_ENABLED_FEATURES)?;

    let features = cstr_buf_to_string(&cmd.enabled_features);
    let has_features = cmd.err == 0 && !features.is_empty();

    if check_only {
        if has_features {
            println!("Supported");
            Ok(())
        } else {
            Err(anyhow!("Unsupported"))
        }
    } else if has_features {
        print!("{features}");
        Ok(())
    } else {
        Err(anyhow!("Invalid (Error: {})", cmd.err))
    }
}

/// 照搬 Next susfsd.rs:109-114。err==126 即内核未打 susfs 补丁，给出明确降级提示。
fn check_unsupported(err: i32, cmd: u32) -> Result<()> {
    if err == ERR_CMD_NOT_SUPPORTED {
        return Err(anyhow!(
            "CMD: '0x{:x}', SUSFS operation not supported, please enable it in kernel",
            cmd
        ));
    }
    Ok(())
}
