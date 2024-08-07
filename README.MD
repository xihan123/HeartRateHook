![HeartRateHook](https://socialify.git.ci/xihan123/HeartRateHook/image?description=1&font=Inter&forks=1&issues=1&language=1&name=1&owner=1&pattern=Plus&pulls=1&stargazers=1&theme=Light)

![above](https://img.shields.io/badge/Android-8.0%20or%20above-brightgreen.svg)
[![Android CI](https://github.com/xihan123/HeartRateHook/actions/workflows/build.yml/badge.svg)](https://github.com/xihan123/HeartRateHook/actions/workflows/build.yml)
[![Latest Release](https://img.shields.io/github/release/xihan123/HeartRateHook.svg)](https://github.com/xihan123/HeartRateHook/releases)
![downloads](https://img.shields.io/github/downloads/xihan123/HeartRateHook/total)
[![license](https://img.shields.io/github/license/xihan123/HeartRateHook.svg)](https://www.gnu.org/licenses/gpl-3.0.html)

---

# HeartRateHook

## 项目简介

HeartRateHook 是一个用于监控和更新心率数据的项目

本项目是 [miband-heart-rate-bot](https://github.com/xihan123/miband-heart-rate-bot) 配套的 Android 项目

## 适配应用

- Zepp Life-6.12.0
- 小米运动健康-3.32.0

## 主要功能

- 监听心率数据更新
- 去抖处理心率数据
- 通过 API 接口发送心率数据

## 项目结构

- `keystore.properties`: 包含签名配置，如签名密码
- `local.properties`: 包含本地配置，如 Android SDK 路径

## 安装与使用

1. 克隆项目到本地：

    ```sh
    git clone https://github.com/xihan123/HeartRateHook.git
    ```

2. 使用 Android Studio 打开项目

3. 配置 `local.properties` 文件中的 SDK 路径，以及 `keystore.properties` 文件中的签名配置

4. 编译发布版：

    ```sh
    ./gradlew synthesizeDistReleaseApksCI
    ```

5. 运行项目

## 主要代码说明

### Di.kt

使用前需要配置 `Di.kt` 文件中的 `provideKtorfit` 方法，将 `baseUrl` 修改为你的服务器地址

```kotlin
private fun provideKtorfit(httpClient: HttpClient) = Ktorfit.Builder()
    .httpClient(httpClient)
    .baseUrl("http://192.168.43.100:8000/") // TODO: 这里是你的服务器地址
    .build()
```

## 贡献

欢迎提交问题和请求，也欢迎贡献代码，请 fork 本项目并提交 pull request

## 许可证

本项目采用 GPLV3.0 许可证，详细信息请参阅 LICENSE 文件

## 打赏

## 如果觉得这个模块对您有用，可扫描下方二维码或者 [爱发电](https://afdian.net/a/xihan123) 随意打赏,要是能打赏个 10.24 🐵就太👍了。您的支持就是我更新的动力

<table>
<tr>
<td align=center>支付宝</td>
<td align=center>微信</td>
</tr>

<tr>
<td>
<img src="https://github.moeyy.xyz/https://raw.githubusercontent.com/xihan123/QDReadHook/master/Screenshots/zfb.jpg" width=270 >
</td>
<td>
<img src="https://github.moeyy.xyz/https://raw.githubusercontent.com/xihan123/QDReadHook/master/Screenshots/wx.png" width=270 >
</td>
</tr>

</table>

---

## Star History

[![Star History Chart](https://api.star-history.com/svg?repos=xihan123/HeartRateHook&type=Date)](https://star-history.com/#xihan123/HeartRateHook&Date)