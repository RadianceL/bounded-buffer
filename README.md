# bounded-buffer

## 基础架构

由核心容器`AbstractBoundedBufferContainer`来保证容器运行状态，
核心容器为抽象类，仅保证运行状态，规定主要容器主要逻辑

其真实容器存放于子类，由子类（如`WaitNotifyBoundedBufferContainer`）维护真实容器

重写其中`*0`结尾的方法扩展核心容器`AbstractBoundedBufferContainer`的生命周期方法

容器运行需要外部传入`Custom` 和 `Producer`来实现真实生产消费方法

## 使用方法



## 运行需求

最低JDK版本号为1.8，内部实现运用部分JDK8的写法，例如Lambda表达式，::取方法

IDE Lombok插件，lombok编译期插入代码，如果不安装插件运行ok，但项目中无法取到方法，大量标红

## 版本号

- v0.0.1 基础版本，提供基础wait/notify方式实现的保证器