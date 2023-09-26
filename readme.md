# 推送客户端

API文档请移步 [服务端仓库](https://github.com/PeanutMelonSeedBigAlmond/EveryPush.Server)

## 特性

1. 使用 FCM 进行消息推送
2. 支持 纯文本，图片和 markdown

## 注意

1. 使用前确保设备安装了 Google play 服务，否则 app 将崩溃
2. 通知权限为必须权限

## 自建服务端与客户端

1. 在 [firebase 控制台](https://console.firebase.google.com/) 中添加项目，上传自己的包名和签名 SHA 1
2. 在 `项目设置/常规`中，下载 `google-services.json`，放在 app 目录下
3. 编译项目，并使用第 1 步中的签名对 apk 签名