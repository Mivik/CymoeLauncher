<h1 align="center">CymoeLauncher</h1>
<p>
  <img alt="Version" src="https://img.shields.io/badge/version-1.0.0-blue.svg?cacheSeconds=2592000" />
  <a href="./LICENSE.md" target="_blank">
    <img alt="License: GPL--3.0" src="https://img.shields.io/badge/License-GPL--3.0-yellow.svg" />
  </a>
</p>

> 龙渊代理的 Cytus II 启动器

## ▶️ 使用

你可以从 [release界面](https://github.com/Mivik/CymoeLauncher/releases) 下载 **CymoeLauncher** 的最新版本。

同时，你也可以选择从源码自行构造 **CymoeLauncher**。

## 🔨 构建

~~众所周知，~~ Assembly-CSharp.dll 是一个 Unity 程序的主要代码部分。

为了构建 **CymoeLauncher**，你需要手动更改游戏中提取出的 Assembly-CSharp.dll 并将它放置在 `assets` 文件夹中。

更改建议使用 [dnSpy](https://github.com/0xd4d/dnSpy) 。你需要做的是人工地把 `app/src/main/csharp` 文件夹中所有指定的方法一一替换，很抱歉没有做出自动编译的脚本。

注意 csharp 文件夹中的 Cymoe.cs 是游戏中并不存在的类，需要手工添加。

构建好 Assembly-CSharp.dll 后，你就可以像构建正常 Android 应用一样构建 **CymoeLauncher**　了。

## 🖼️ 预览

![CymoeLauncher 主界面](https://s1.ax1x.com/2020/04/04/Gwzrn0.jpg)

![游戏内悬浮窗](https://s1.ax1x.com/2020/04/04/Gwxhef.jpg)

![Drag 被替换为 Click](https://s1.ax1x.com/2020/04/04/Gwx4w8.jpg)

## 作者

👤 **Mivik**

* 个人主页: https://mivik.gitee.io
* Github: [@Mivik](https://github.com/Mivik)

## 🤝 贡献

欢迎提 [issue](https://github.com/Mivik/issues) 和 PR！ 

## 同时...

如果这个项目对你有帮助，那就点个 ⭐️ 吧！

## 📝 许可证

Copyright © 2020 [Mivik](https://github.com/Mivik).

This project is [GPL--3.0](./LICENSE.md) licensed.
