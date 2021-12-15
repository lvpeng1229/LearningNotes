#  配置操作
### 全局配置

```
对全局用户的所有仓库有效（最常用）
git config --global user.name '你的名字'  
git config --global user.email '你的邮箱'

对当前仓库用户有效（不常用）
git config --local user.name '你的名字'
git config --local user.email '你的邮箱'

对系统所有用户有效（根本就不用）
git config --system user.name '你的名字'
git config --system user.email '你的邮箱'


--------------------------例如--------------------------

git config --global user.name 'lvpeng'
git config --global user.email 'lvpeng1229@gmail.com'


```

### 清除设置

```
git config --unset --global 要删除的全局配置项
git config --unset --local  要删除的当前仓库配置项
git config --unset --system 要删除的系统配置项

--------------------------例如--------------------------
git config --unset --global user.name
git config --unset --global user.email

```

### 查看配置

```
git config --list           查看当前配置
git config --list --global  查看全局配置
git config --list --local   查看当前仓库配置
git config --list --system  查看系统配置项


```
### 初始化仓库

```
cd 项目代码所在的文件夹
git init
```


### 建立客户端与远程服务端的连接
```
git remote add origin 远程仓库的地址

--------------------------例如--------------------------
git remote add origin git@server-name:path/repo-name.git
```


### 连接建立完成之后，把本地代码推送到服务端
我们第一次推送master分支时，加上了-u参数，Git不但会把本地的master分支内容推送的远程新的master分支，还会把本地的master分支和远程的master分支关联起来，在以后的推送或者拉取时就可以去掉 “-u” 简化命令执行了。

```
git push -u origin 本地分支名

--------------------------例如--------------------------
git push -u origin master
```



#  本地操作

### 查看变更情况
```
git status
```

### 将当前目录及其子目录下所有变更都加入到暂存区
```
git add .
```

### 将仓库内所有变更都加入到暂存区
```
git add -A
```

### 将文件的修改、文件的删除，添加到暂存区（不常用）
```
git add -u

```

### 将指定文件添加到暂存区

```
git add 文件1 文件2 文件3

```

### 直接将修改后的文件提交到本地仓库
```
git commit -a -m "说明信息"   或者   git commit -am "说明信息"
```

### 变更文件名
```
git mv 原文件名 新修改后的文件名
```

### 查看当前目录下的所有文件 
```
ls -al 
```


### 比较工作区和暂存区的所有差异
```
git diff
```

### 比较某个文件在工作区和暂存区的差异
```
git diff 文件
```

### 比较暂存区和 HEAD 的所有差异
```
git diff --cached
```

### 比较某个文件在暂存区和 HEAD 的差异
```
git diff --cached 文件
```

### 比较某文件工作区和 HEAD 的差异
```
git diff HEAD 文件
```

### 查看所有提交记录
```
git log
```

### 查看显示就近的 n 个 commit
```
git log -n
```

### 当前分支 commit 用一行显示，该命令会使commitID变的简短
```
git log --oneline
  
或者  

git log --pretty=oneline --abbrev-commit 
```

### 当前分支各个 commit 用一行显示，commitID 比较长
```
git log --pretty=oneline
```

#### 查看commit之间的父子关系
```
git log --pretty=raw 
git log --pretty=raw --abbrev-commit   //缩短commitId
```

### 可以看到分支合并图
```
git log --graph --pretty=oneline 
```

### 显示所有本地和远程的所有提交记录
```
git reflog
```

### 查看所有分支的历史
```
git log --all 
```

### 查看图形化的 log 地址
```
git log --all --graph
```

### 查看所有图形化的 log 地址，commitId变短
```
git log --oneline --graph --all
```
  
### 查看最近的四条简洁历史
```
git log --oneline -n4
```

### 查看所有分支最近 4 条单行的图形化历史
```
git log --oneline --all -n4 --graph

```


### 某文件各行最后修改对应的 commit 以及作者 (没鸡毛用)
```
git blame 文件 
```   


### 用 difftool 比较任意两个 commit 的差异
```
git difftool commitId1 commitId2 
```   



--- 
> **git reset 命令既可以回退版本，也可以把暂存区的修改回退到工作区。当我们用 HEAD 时，表示最新的版本。**
--- 



### 将暂存区和工作区所有文件回退到和 HEAD 一样
```
git reset --hard
``` 

### 将暂存区和工作区所有文件回退到指定的某个提交点的版本（版本穿越）
```
git reset --hard commit_id

如果不知道commitID，可以使用git reflog查看
```


### 回退到上一个版本
```
git reset --hard HEAD~1
git reset --hard HEAD^（上一个版本）
git reset --hard HEAD^^（上上个版本）
```

--- 
### 对于几个区域的操作：
1. 工作区 --> 暂存区 ：git add
1. 暂存区 --> 本地仓库 ：git commit
1. 暂存区 --> 工作区 ：git reset HEAD fileName || git restore --staged fileName
--- 



### 将保存在暂存区中的所有文件踢回到工作区中
```
git reset head（HEAD）
``` 

### 撤销修改：情况一，在工作区中发生了修改，但是还没提交到暂存区（没有add和commit）
```
git checkout -- fileName
或者（二者效果等同）
git restore fileName

```


### 撤销修改：情况二，已经添加到了暂存区，但是还没有提交（已add，还没有commit）
```
1、将保存在暂存区中的文件踢回到工作区中 
git reset head（HEAD）fileName
或者（二者效果等同）
git restore --staged fileName


2、随后还要恢复已有内容：
git checkout -- fileName
或者（二者效果等同）
git restore fileName
```

### 比较暂存区与工作区的文件变化
```
git diff
```


### 比较暂存区与 HEAD 的文件变化
```
git diff --cached
```

### 查看哪些文件没被 Git 管控
```
git ls-files --others
```


### 将当前分支暂时挂起
```
git stash
```

### 恢复挂起的工作区
```
1、分步完成：
git stash apply  恢复暂时挂起的工作区
git stash drop   清除暂时挂起的工作区

2、一步完成 
git stash pop 从暂挂区中恢复工作区后删除暂挂区
```
### 查看当前挂起的分支
```
git stash list
```

### 取回某次 stash 的变更
```
git stash pop stash@{数字n}
```

### 修改最后一次提交信息
```
git commit --amend

该命令会进入vim模式
1、按 “i” 键进入编辑模式。会在终端的左下角出现 ”-- NSERT --” 字样。
2、修改提交信息。
3、按 “ESC” 键退出编辑模式。
4、输入法切换到英文状态下，输入 “:wq”，保存并退出。
```


### 修改历史版本中某次提交信息
本案例中是想修改 **e35d8b3 加入网络请求** 这个提交记录。操作如下：

1、查询提交记录
```
git log --oneline
```

![image.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/078eb6f400dc4de5a21e8ab05f16a7df~tplv-k3u1fbpfcp-watermark.image?)

2、git rebase -i 要修改的commitId的父亲commitId。这里的 commitId 就应该是 **a05a00e**

```
git rebase -i a05a00e
```


3、然后会弹出进入vim模式，按“i” 进入编辑模式。目的是修改策略，将第一行修改为 r 后。按 "ESC" 退出编辑模式，输入 “:wq” 后保存并退出。


![image.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/7574301361304e06805333bf5a5603d1~tplv-k3u1fbpfcp-watermark.image?)


4、回车后，再次进入 vim 编辑模式操作，修改提交记录

![image.png](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/bd1b4b350f4d4562ae2234a1b45b244e~tplv-k3u1fbpfcp-watermark.image?)

5、再次回车，修改成功！

![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/716ea92a0a234334832786682c6f857b~tplv-k3u1fbpfcp-watermark.image?)

### 把连续多个commit整理成一个

// TODO 

### 把不连续多个commit整理成一个

// TODO 


### 选择一个commit，合并进当前分支
```
git cherry-pick commitId
```

### 文件删除恢复操作
1. 在磁盘中删除了某个文件，然后执行了add&commit 操作，则分支上的文件就彻底没有了。此时如果认为该文件删错了，则使用版本穿越的方式进行恢复。以上操作是：删除已经被提交的操作，然后恢复回来。
```
git reset --hard commitID
```

2. 如果没有add和commit，可以按照**撤销修改：情况一**方式处理

3. 如果已经add，并没有commit，可以按照**撤销修改：情况二**方式处理


### 比较某文件两次不同提交的差异
```
git diff <commit_id1> <commit_id2> -- <file_name>

--------------------------例如--------------------------
git diff 8740378 303df3b -- app/src/main/java/com/kugou/MainActivity.java
```

### 比较某文件两个不同分支的差异
```
git diff <branch_1> <branch_2> -- <file_name>

--------------------------例如--------------------------
git diff master dev -- app/src/main/java/com/kugou/MainActivity.java
```


### 删除文件，这种方式是同时删除工作区和暂存区的文件
```
git rm 文件名

--------------------------例如--------------------------
git rm app/src/main/java/com/kugou/MainActivity.java
```



# 分支操作

### 创建分支（基于当前分支）
```
git branch 分支名
```

### 切换到指定分支
```
git checkout 分支名
或者
git switch 分支名
```

### 创建并且切换分支
```
git checkout -b 分支名
或者
git switch -c 分支名
```

### 基于指定分支创建新分支
```
git branch 新分支 指定分支
```

### 基于某个 commitId 创建新分支
```
git branch 新分支 某个commitId
```


### 删除分支
```
在删除分支之前，首先要切换到其他分支，然后执行下面的命令才能执行成功
git branch -d 分支名
```

### 强制删除分支
```
在删除分支之前，首先要切换到其他分支，然后执行下面的命令才能执行成功
git branch -D 分支名
```

### 查看当前分支
```
git branch       // 只显示分支名
git branch -v    // 显示分支名和最后一次提交记录
```

### 查看本地和远程分支
```
git branch -a     // 只显示分支名
git branch -av    // 显示分支名和最后一次提交记录
```

### 查看远程分支
```
git branch -rv
```

### 将A分支合并到当前分支并且为此次合并创建commitId
```
首先要切换到当前分支，然后才可以执行下面的命令合并A分支。不可以在A分支上mergeA分支
git merge A分支 
```

### 将 A 分支合入到 B 分支中且为 merge 创建 commitId
```
git merge A分支 B分支
```

### 将当前分支(feat)基于B分支（bugfix）做 rebase，以便将B分支（bugfix）合入到当前分支
```
前   提：当前分支为feat，想要把bugfix分支变基到feat上。
操作步骤：切换到feat分支（git checkout feat），然后执行（git rebase fixbug）
git rebase B分支
```


### 将 A 分支基于 B 分支做 rebase，以便将 B 分支合入到 A 分支
```
git rebase B分支 A分支
```

#  配置操作
### 全局配置

```
对全局用户的所有仓库有效（最常用）
git config --global user.name '你的名字'  
git config --global user.email '你的邮箱'

对当前仓库用户有效（不常用）
git config --local user.name '你的名字'
git config --local user.email '你的邮箱'

对系统所有用户有效（根本就不用）
git config --system user.name '你的名字'
git config --system user.email '你的邮箱'


--------------------------例如--------------------------

git config --global user.name 'lvpeng'
git config --global user.email 'lvpeng1229@gmail.com'


```



### 清除设置

```
git config --unset --global 要删除的全局配置项
git config --unset --local  要删除的当前仓库配置项
git config --unset --system 要删除的系统配置项

--------------------------例如--------------------------
git config --unset --global user.name
git config --unset --global user.email

```



### 查看配置

```
git config --list           查看当前配置
git config --list --global  查看全局配置
git config --list --local   查看当前仓库配置
git config --list --system  查看系统配置项


```


### 初始化仓库

```
cd 项目代码所在的文件夹
git init
```


### 建立客户端与远程服务端的连接
```
git remote add origin 远程仓库的地址

--------------------------例如--------------------------
git remote add origin git@server-name:path/repo-name.git
```


### 连接建立完成之后，把本地代码推送到服务端
我们第一次推送master分支时，加上了-u参数，Git不但会把本地的master分支内容推送的远程新的master分支，还会把本地的master分支和远程的master分支关联起来，在以后的推送或者拉取时就可以去掉 “-u” 简化命令执行了。

```
git push -u origin 本地分支名

--------------------------例如--------------------------
git push -u origin master
```



#  本地操作

### 查看变更情况
```
git status
```

### 将当前目录及其子目录下所有变更都加入到暂存区
```
git add .
```

### 将仓库内所有变更都加入到暂存区
```
git add -A
```

### 将文件的修改、文件的删除，添加到暂存区（不常用）
```
git add -u

```

### 将指定文件添加到暂存区

```
git add 文件1 文件2 文件3

```

### 直接将修改后的文件提交到本地仓库
```
git commit -a -m "说明信息"   或者   git commit -am "说明信息"
```

### 变更文件名
```
git mv 原文件名 新修改后的文件名
```

### 查看当前目录下的所有文件 
```
ls -al 
```


### 比较工作区和暂存区的所有差异
```
git diff
```

### 比较某个文件在工作区和暂存区的差异
```
git diff 文件
```

### 比较暂存区和 HEAD 的所有差异
```
git diff --cached
```

### 比较某个文件在暂存区和 HEAD 的差异
```
git diff --cached 文件
```

### 比较某文件工作区和 HEAD 的差异
```
git diff HEAD 文件
```

### 查看所有提交记录
```
git log
```

### 查看显示就近的 n 个 commit
```
git log -n
```

### 当前分支 commit 用一行显示，该命令会使commitID变的简短
```
git log --oneline
  
或者  

git log --pretty=oneline --abbrev-commit 
```

### 当前分支各个 commit 用一行显示，commitID 比较长
```
git log --pretty=oneline
```

### 可以看到分支合并图
```
git log --graph --pretty=oneline 
```

### 显示所有本地和远程的所有提交记录
```
git reflog
```

### 查看所有分支的历史
```
git log --all 
```

### 查看图形化的 log 地址
```
git log --all --graph
```

### 查看所有图形化的 log 地址，commitId变短
```
git log --oneline --graph --all
```
  
### 查看最近的四条简洁历史
```
git log --oneline -n4
```

### 查看所有分支最近 4 条单行的图形化历史
```
git log --oneline --all -n4 --graph

```


### 某文件各行最后修改对应的 commit 以及作者 (没鸡毛用)
```
git blame 文件 
```   


### 用 difftool 比较任意两个 commit 的差异
```
git difftool commitId1 commitId2 
```   



--- 
> **git reset 命令既可以回退版本，也可以把暂存区的修改回退到工作区。当我们用 HEAD 时，表示最新的版本。**
--- 



### 将暂存区和工作区所有文件回退到和 HEAD 一样
```
git reset --hard
``` 

### 将暂存区和工作区所有文件回退到指定的某个提交点的版本（版本穿越）
```
git reset --hard commit_id

如果不知道commitID，可以使用git reflog查看
```


### 回退到上一个版本
```
git reset --hard HEAD~1
git reset --hard HEAD^（上一个版本）
git reset --hard HEAD^^（上上个版本）
```

--- 
### 对于几个区域的操作：
1. 工作区 --> 暂存区 ：git add
1. 暂存区 --> 本地仓库 ：git commit
1. 暂存区 --> 工作区 ：git reset HEAD fileName || git restore --staged fileName
--- 



### 将保存在暂存区中的所有文件踢回到工作区中
```
git reset head（HEAD）
``` 

### 撤销修改：情况一，在工作区中发生了修改，但是还没提交到暂存区（没有add和commit）
```
git checkout -- fileName
或者（二者效果等同）
git restore fileName

```


### 撤销修改：情况二，已经添加到了暂存区，但是还没有提交（已add，还没有commit）
```
1、将保存在暂存区中的文件踢回到工作区中 
git reset head（HEAD）fileName
或者（二者效果等同）
git restore --staged fileName


2、随后还要恢复已有内容：
git checkout -- fileName
或者（二者效果等同）
git restore fileName
```

### 比较暂存区与工作区的文件变化
```
git diff
```


### 比较暂存区与 HEAD 的文件变化
```
git diff --cached
```

### 查看哪些文件没被 Git 管控
```
git ls-files --others
```


### 将当前分支暂时挂起
```
git stash
```

### 恢复挂起的工作区
```
1、分步完成：
git stash apply  恢复暂时挂起的工作区
git stash drop   清除暂时挂起的工作区

2、一步完成 
git stash pop 从暂挂区中恢复工作区后删除暂挂区
```
### 查看当前挂起的分支
```
git stash list
```

### 取回某次 stash 的变更
```
git stash pop stash@{数字n}
```

### 修改最后一次提交信息
```
git commit --amend

该命令会进入vim模式
1、按 “i” 键进入编辑模式。会在终端的左下角出现 ”-- NSERT --” 字样。
2、修改提交信息。
3、按 “ESC” 键退出编辑模式。
4、输入法切换到英文状态下，输入 “:wq”，保存并退出。
```


### 修改历史版本中某次提交信息
本案例中是想修改 **e35d8b3 加入网络请求** 这个提交记录。操作如下：

1、查询提交记录
```
git log --oneline
```

![image.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/078eb6f400dc4de5a21e8ab05f16a7df~tplv-k3u1fbpfcp-watermark.image?)

2、git rebase -i 要修改的commitId的父亲commitId。这里的 commitId 就应该是 **a05a00e**

```
git rebase -i a05a00e
```


3、然后会弹出进入vim模式，按“i” 进入编辑模式。目的是修改策略，将第一行修改为 r 后。按 "ESC" 退出编辑模式，输入 “:wq” 后保存并退出。


![image.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/7574301361304e06805333bf5a5603d1~tplv-k3u1fbpfcp-watermark.image?)


4、回车后，再次进入 vim 编辑模式操作，修改提交记录

![image.png](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/bd1b4b350f4d4562ae2234a1b45b244e~tplv-k3u1fbpfcp-watermark.image?)

5、再次回车，修改成功！

![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/716ea92a0a234334832786682c6f857b~tplv-k3u1fbpfcp-watermark.image?)

### 把连续多个commit整理成一个

// TODO 

### 把不连续多个commit整理成一个

// TODO 

### 文件删除恢复操作
1. 在磁盘中删除了某个文件，然后执行了add&commit 操作，则分支上的文件就彻底没有了。此时如果认为该文件删错了，则使用版本穿越的方式进行恢复。以上操作是：删除已经被提交的操作，然后恢复回来。
```
git reset --hard commitID
```

2. 如果没有add和commit，可以按照**撤销修改：情况一**方式处理

3. 如果已经add，并没有commit，可以按照**撤销修改：情况二**方式处理


### 比较某文件两次不同提交的差异
```
git diff <commit_id1> <commit_id2> -- <file_name>

--------------------------例如--------------------------
git diff 8740378 303df3b -- app/src/main/java/com/kugou/MainActivity.java
```

### 比较某文件两个不同分支的差异
```
git diff <branch_1> <branch_2> -- <file_name>

--------------------------例如--------------------------
git diff master dev -- app/src/main/java/com/kugou/MainActivity.java
```


### 删除文件，这种方式是同时删除工作区和暂存区的文件
```
git rm 文件名

--------------------------例如--------------------------
git rm app/src/main/java/com/kugou/MainActivity.java
```



# 分支操作

### 创建分支（基于当前分支）
```
git branch 分支名
```

### 切换到指定分支
```
git checkout 分支名
或者
git switch 分支名
```

### 创建并且切换分支
```
git checkout -b 分支名
或者
git switch -c 分支名
```

### 基于指定分支创建新分支
```
git branch 新分支 指定分支
```

### 基于某个 commitId 创建新分支
```
git branch 新分支 某个commitId
```


### 删除分支
```
在删除分支之前，首先要切换到其他分支，然后执行下面的命令才能执行成功
git branch -d 分支名
```

### 强制删除分支
```
在删除分支之前，首先要切换到其他分支，然后执行下面的命令才能执行成功
git branch -D 分支名
```

### 查看当前分支
```
git branch       // 只显示分支名
git branch -v    // 显示分支名和最后一次提交记录
```

### 查看本地和远程分支
```
git branch -a     // 只显示分支名
git branch -av    // 显示分支名和最后一次提交记录
```

### 查看远程分支
```
git branch -rv
```

### 将A分支合并到当前分支并且为此次合并创建commitId
```
首先要切换到当前分支，然后才可以执行下面的命令合并A分支。不可以在A分支上mergeA分支
git merge A分支 
```

### 将 A 分支合入到 B 分支中且为 merge 创建 commitId
```
git merge A分支 B分支
```

### 将当前分支(feat)基于B分支（bugfix）做 rebase，以便将B分支（bugfix）合入到当前分支
```
前   提：当前分支为feat，想要把bugfix分支变基到feat上。
操作步骤：切换到feat分支（git checkout feat），然后执行（git rebase fixbug）
git rebase B分支
```


### 将 A 分支基于 B 分支做 rebase，以便将 B 分支合入到 A 分支
```
git rebase B分支 A分支
```


# 标签操作
### 查看已有标签
```
git tag
```

### 新建标签
```
git tag v1.0
```

### 新建带备注标签
```
git tag -a v1.0 -m '前端食堂'
```

### 给指定的 commit 打标签
```
git tag v1.0 commitId
```


### 推送一个本地标签
```
git push origin v1.0
```

### 推送全部未推送过的本地标签
```
git push origin --tags
```

### 删除一个本地标签

```
git tag -d v1.0
```

### 删除一个远端标签

```
git push origin :refs/tags/v1.0
```

# 远端交互

### 查看所有远端仓库

```
git remote -v
```

### 添加远端仓库

```
git remote add url
```

### 删除远端仓库

```
git remote remove remote的名称
```

### 重命名远端仓库

```
git remote rename 旧名称 新名称
```

### 取回特定分支的更新
```
git fetch <远程主机名> <分支名>

-----------比如，取回`origin`主机的`master`分支。-----------
git fetch origin master
```



### 将远端所有分支和标签的变更都拉到本地
```
git fetch remote
```

### 把远端分支的变更拉到本地，且 merge 到本地分支

```
git pull origin 分支名
```

### 将本地分支 push 到远端

```
git push origin 分支名
```

### 删除远端分支

```
git push remote --delete 远端分支名
```

### 拉取所有远端的最新代码
```
git fetch --all
```

### 重命名分支
```
git branch -m 旧分支名 新分支名
```








# 标签操作
### 查看已有标签
```
git tag
```

### 新建标签
```
git tag v1.0
```

### 新建带备注标签
```
git tag -a v1.0 -m '前端食堂'
```

### 给指定的 commit 打标签
```
git tag v1.0 commitId
```


### 推送一个本地标签
```
git push origin v1.0
```

### 推送全部未推送过的本地标签
```
git push origin --tags
```


# 远端交互

### 查看所有远端仓库

```
git remote -v
```

### 添加远端仓库

```
git remote add url
```

### 删除远端仓库

```
git remote remove remote的名称
```

### 重命名远端仓库

```
git remote rename 旧名称 新名称
```

### 将远端所有分支和标签的变更都拉到本地
```
git fetch remote
```

### 把远端分支的变更拉到本地，且 merge 到本地分支（fetch + merge）
```
git fetch origin 分支名 + git merge origin/分支名
```

### 将本地分支 push 到远端

```
git push origin 分支名

--------------------例如--------------------
git push origin master // 将本地master分支推送到远程origin主机的master分支

```

### 删除远端分支

```
git push remote --delete 远端分支名
```

### 拉取所有远端的最新代码
```
$ git fetch --all
```

# git pull

### 将远程origin主机的master分支合并到当前master分支，冒号后面的部分表示当前本地所在的分支
```
git pull命令的作用是，取回远程主机某个分支的更新，再与本地的指定分支合并。它的完整格式稍稍有点复杂。
git pull <远程主机名> <远程分支名>:<本地分支名>
比如，取回`origin`主机的`dev`分支，与本地的`master`分支合并，需要写成下面这样。

git pull origin dev:master

如果远程分支是与当前分支合并，则冒号后面的部分可以省略。
git pull origin next
```

### 允许合并两个不同项目的历史记录
```
git pull origin master --allow-unrelated-histories**
```

### 删除远程分支
```
git push origin -d 分支名
```

### 在本地（当前）分支上合并远程分支
```
git merge origin/master
```

### 在本地master分支上合并远程分支
```
git merge --no-ff origin/develop
```

### 终止本次merge，并回到merge前的状态
```
git merge --abort
```

### 建立追踪关系，在现有分支与指定的远程分支之间
```
git branch --set-upstream [本地分支] [远程分支]


--------------比如----------------
git branch --set-upstream master origin/next

上面命令指定`master`分支追踪`origin/next`分支。
```

### 参考：
https://www.ruanyifeng.com/blog/2014/06/git_remote.html

https://www.ruanyifeng.com/blog/2015/08/git-use-process.html

https://juejin.cn/post/6844903598522908686#heading-4

https://juejin.cn/post/6844903598522908686#heading-2

https://juejin.cn/post/6844903546104135694#heading-0

https://juejin.cn/post/6974184935804534815#heading-6

https://blog.csdn.net/zaishuiyifangxym/category_9991200.html

https://juejin.cn/post/6844903586120335367

https://juejin.cn/post/6844903635533594632#heading-1

https://www.liaoxuefeng.com/wiki/896043488029600/896954117292416
