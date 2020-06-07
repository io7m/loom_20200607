```
$ siege -c 512 -r 100 http://localhost:9090/tasks
```

```
#
# A fatal error has been detected by the Java Runtime Environment:
#
#  SIGSEGV (0xb) at pc=0x00007f77568baf72, pid=340507, tid=341090
#
# JRE version: OpenJDK Runtime Environment (15.0) (build 15-internal+0-adhoc.jenkins.loom)
# Java VM: OpenJDK 64-Bit Server VM (15-internal+0-adhoc.jenkins.loom, mixed mode, sharing, tiered, compressed oops, g1 gc, linux-amd64)
# Problematic frame:
# V  [libjvm.so+0x745f72]  frame::sender_for_interpreter_frame(RegisterMap*) const+0x32
#
# Core dump will be written. Default location: Core dumps may be processed with "/usr/lib/systemd/systemd-coredump %P %u %g %s %t %c %h" (or dumping to /home/rm/git/com.github/io7m/loom-20200607/core.340507)
#
# An error report file with more information is saved as:
# /home/rm/git/com.github/io7m/loom-20200607/hs_err_pid340507.log
#
# If you would like to submit a bug report, please visit:
#   https://bugreport.java.com/bugreport/crash.jsp
#
```
