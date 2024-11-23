#!/bin/bash

# 检查Java是否安装
if ! command -v java &> /dev/null; then
    echo "错误：未安装Java运行时环境（JRE）"
    echo "请运行："
    echo "sudo apt update"
    echo "sudo apt install default-jdk"
    exit 1
fi

# 检查javac是否安装
if ! command -v javac &> /dev/null; then
    echo "错误：未安装Java编译器（JDK）"
    echo "请运行："
    echo "sudo apt update"
    echo "sudo apt install default-jdk"
    exit 1
fi

# 检查图片目录是否存在
if [ ! -d "images" ]; then
    mkdir -p images
    echo "创建images目录"
fi

# 检查必要的图片文件
for img in "plane.png" "bullet.png" "redpacket.png"; do
    if [ ! -f "images/$img" ]; then
        echo "警告：缺少图片文件 images/$img"
        # 创建一个简单的彩色方块作为临时图片
        convert -size 50x30 xc:blue "images/$img"
    fi
done

# 编译和运行
echo "编译Java文件..."
javac *.java
if [ $? -eq 0 ]; then
    echo "编译成功，启动游戏..."
    java PlaneGame
else
    echo "编译失败！"
    exit 1
fi
