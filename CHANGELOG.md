# Changelog

## [1.3.0](https://github.com/xihan123/HeartRateHook/compare/v1.2.1...v1.3.0) (2025-07-02)


### Features

* allow user-defined server address in Cookie mode ([1b92c02](https://github.com/xihan123/HeartRateHook/commit/1b92c026a023fe66e9fc3109e44c63f2faa5d543))

## [1.2.1](https://github.com/xihan123/HeartRateHook/compare/v1.2.0...v1.2.1) (2025-04-04)


### Bug Fixes

* 上报时间5秒改为1分钟 ([25b7a72](https://github.com/xihan123/HeartRateHook/commit/25b7a72b495440ec9d61d805c073603d7e6e8abd))

## [1.2.0](https://github.com/xihan123/HeartRateHook/compare/v1.1.2...v1.2.0) (2025-04-04)


### Features

* **utils:** 新增 MiHealthPackage 工具类 ([78b3bdd](https://github.com/xihan123/HeartRateHook/commit/78b3bdd408737b61d016c2abdabfd12ac962b782))
* **utils:** 新增工具类 Utils.kt ([603d310](https://github.com/xihan123/HeartRateHook/commit/603d310ff845a7c4ed095f7ac295588e8cc3fd31))
* **utils:** 添加 Settings 对象用于持久化存储配置信息 ([1ce15c7](https://github.com/xihan123/HeartRateHook/commit/1ce15c754ceb9c4e7eb7685c53338eabf1cefd09))
* **utils:** 添加 ToastUtil 工具类 ([1f44027](https://github.com/xihan123/HeartRateHook/commit/1f44027e835fc9320290dacf08eb83480888d63a))
* **utils:** 添加安全的协程作用域工具类 ([b0f7ccf](https://github.com/xihan123/HeartRateHook/commit/b0f7ccfa892dff26727178e1c798588ffaef6492))
* 添加 Kotlin Xposed 辅助类 ([b57e9e6](https://github.com/xihan123/HeartRateHook/commit/b57e9e6d59443ca212d76af45dc7701415c8d2ab))
* 添加日志工具类 ([b28d77c](https://github.com/xihan123/HeartRateHook/commit/b28d77c943a98afc1deb7e007b8a5d130d047925))
* 添加自定义 Cookie 存储类 ([24ab6d6](https://github.com/xihan123/HeartRateHook/commit/24ab6d6785e9f7c9ef493696581b0a58b3364f00))
* 重构 Ktor客户端并添加登录功能 ([070066b](https://github.com/xihan123/HeartRateHook/commit/070066bd88ee8fdacc9a567b1a3c0e6d646ec3c5))


### Performance Improvements

* 优化 ProGuard 规则以减小 APK 体积 ([5fbe734](https://github.com/xihan123/HeartRateHook/commit/5fbe734a3bf431273852bed89f415dc01bda84ac))

## [1.1.1](https://github.com/xihan123/HeartRateHook/compare/v1.1.0...v1.1.1) (2025-02-17)


### Miscellaneous

* 移除 Dependabot 配置文件 ([d46ec36](https://github.com/xihan123/HeartRateHook/commit/d46ec36ac74250afff78fe9f49c20d1e87293567))


### Refactoring

* **#6:** 优化心率数据发送逻辑 ([a4fbad5](https://github.com/xihan123/HeartRateHook/commit/a4fbad59dd67e1e27de5605b5da13c2d387a1f61))


### Build

* 更新项目依赖版本 ([7d45d28](https://github.com/xihan123/HeartRateHook/commit/7d45d2832509480aea2fdbc86601313bc1f89766))
* 添加 Renovate 配置文件 ([1e99a63](https://github.com/xihan123/HeartRateHook/commit/1e99a639c1ed7654f1ff417ccc52921f3c22979b))

## [1.1.0](https://github.com/xihan123/HeartRateHook/compare/v1.0.1...v1.1.0) (2025-02-13)


### Features

* **Utils:** 优化服务器地址设置功能 ([2c3e776](https://github.com/xihan123/HeartRateHook/commit/2c3e776fcbf65e785dcf5e3a7489452613899073))
* 增加Activity实例到ImageView点击事件 ([e21c050](https://github.com/xihan123/HeartRateHook/commit/e21c050f7efa3e78765a13b995b8eb8672185319))


### Refactoring

* **app:** 重构心率数据流处理逻辑 ([eba872a](https://github.com/xihan123/HeartRateHook/commit/eba872aadc9e292803348201e954bacf2c98439f))


### Build

* 更新项目依赖版本 ([b8e3fa4](https://github.com/xihan123/HeartRateHook/commit/b8e3fa4249a3ee5424a886ae0f44e086a5cae973))
* 添加dependabot配置以自动更新依赖 ([c7fa783](https://github.com/xihan123/HeartRateHook/commit/c7fa7832a6425635bec34559f5204de31be04c4e))
* 添加安装后重启功能并简化根构建脚本 ([061d05e](https://github.com/xihan123/HeartRateHook/commit/061d05e74e06a2ee3951329cc7e1ff4166e26afb))

## [1.0.1](https://github.com/xihan123/HeartRateHook/compare/v1.0.0...v1.0.1) (2024-12-22)


### Build

* 升级 Gradle到 8.12 版本 ([a422710](https://github.com/xihan123/HeartRateHook/commit/a422710aa7e0721391ad46993126a4e160726290))
* 更新项目依赖版本 ([074c53a](https://github.com/xihan123/HeartRateHook/commit/074c53a7d654f62bfcb40b0dabfd1a8b56465ac4))
* 添加停止 Mi Life 和 Zepp Life 应用的任务 ([afafa4d](https://github.com/xihan123/HeartRateHook/commit/afafa4d0878ff2d5aeb0b8a2b42163b75de757a3))

## 1.0.0 (2024-08-11)


### Features

* 反射获取参数 ([1f53dea](https://github.com/xihan123/HeartRateHook/commit/1f53dea8272a4e6221308b9bd00df9ce87b25f33))
* 支持动态设置服务器地址(修改后需要重启应用) ([6a7486d](https://github.com/xihan123/HeartRateHook/commit/6a7486de466ae8f6163dd4306fdd99333e021a0d))


### Bug Fixes

* `小米运动健康`支持后台上报 ([cb723c9](https://github.com/xihan123/HeartRateHook/commit/cb723c934f92c73060e8a20b0363ad714f6501c0))


### Docs

* 更新文档 ([15595e4](https://github.com/xihan123/HeartRateHook/commit/15595e4929fe4597ca88deb834a3ffc4ea4bf9ca))
* 更新文档 ([916d996](https://github.com/xihan123/HeartRateHook/commit/916d996a58885c20028db4ecdd2a50f05ccdbfd1))
* 更新文档 ([b899b28](https://github.com/xihan123/HeartRateHook/commit/b899b283bb8858ffeecdb0c23418915e9092bef3))


### CI

* 添加自动发布和自动编译版本 ([5212401](https://github.com/xihan123/HeartRateHook/commit/52124015e420526383bdfe6114e6a23a502a280b))


### Miscellaneous

* 初次提交 ([6611313](https://github.com/xihan123/HeartRateHook/commit/6611313e8442d73f2a4c15b531a2b38ed09e888d))
* 更新依赖 ([0b87c39](https://github.com/xihan123/HeartRateHook/commit/0b87c391be8eaeea48a13a6910b833ef7c67ee58))
* 更新版本 ([473cf3d](https://github.com/xihan123/HeartRateHook/commit/473cf3df973c769205da83931ec2a9e97a2fb3eb))
