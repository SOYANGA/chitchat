# :family_man_woman_girl_boy:  chitchat

![项目状态](https://img.shields.io/badge/ChitChat-doing-green.svg)![项目进度](http://progressed.io/bar/20?title=progress)

## 项目描述

本项目项目名中文翻译过来为“**唠嗑**”，本项目主要功能是可以实现多人网络聊天。附加的功能如下。（由于自身学习原因，目前并未有完整实现此项目的个人技术能力，先根据自身当前情况，做出一个小东西。也是对学习的多线程、集合、数据库的简单运用。之后会按照以下功能完整的实现！:star2:）

- 群聊

- 私聊
- 远程传输文件
- 可从个人兴趣来进行分组等

## 目前项目的基本实现

- :arrows_clockwise:   利用java中Socket类实现服务器与客户端的连接，并获取客户端，服务器的输入输出流。

- :eyes:  利用java中map存放用户信息，实现用户唯一性 。
- :writing_hand:利用多线程来拆解客户端的读写两个过程。 
- :left_right_arrow:利用线程池实现服务器对多个客户端的连接、调度、转发信息、设置最大连接数等。



## 目前要做的优化

- :floppy_disk:  利用数据库存储客户端信息，使客户端信息得以长时间保存。

- :couple_with_heart_woman_man: 服务去可以根据客户端的兴趣爱好实现分组进行唠嗑，客户端也可以自行进行组合实现自定义群聊。

:computer:（目标别定太多，先花时间去学习，学习！！有能力实现起来就不吃力了）