# example bndtools eclipse workspace
This repository contains an example workspace including minimal repositories for development of eclipse products.

## How to run

visit the [Releases Page](https://github.com/A7exSchin/EclipseInstallationManager/releases) and downlaod the latest `eim.jar`.

Execute the JAR with

```
java -jar eim.jar
```

## Current capabilitites

The EIM is currently accessed by the GoGo Shell via a command. Type `help` to see that the command `startProcess <Command> <Working Directory> <Arguments[]>` is available.
Currently any process can be started with this command, given any working directory and set of arguments.
For example:

```
startProcess "C:\eclipse\eclipse.exe" "C:\eclipse\" "ws=D:\workspaces\projectA"
```

This command will start an Eclipse with the argument `ws`, which results in the Eclipse IDE instance to be started with the given workspace.
