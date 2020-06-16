TCP三次握手？
SYN=1,seq=x               SYN-SENT
SYN=1,ACK=1,seq=y,ack=x+1 SYN-RCVD
ACK=1,seq=x+1,ack=y+1     ESTABLISHED

TCP四次挥手？ 
FIN=1,seq=u               FIN_WAIT_1
ACK=1,seq=v,ack=u+1       CLOSE-WAIT
FIN=1,ACK=1,seq=w,ack=u+1 LAST-ACK
ACK=1,seq=u+1,ack=w+1     TIME-WAIT

ClassLoader 类型？
  Java  类加载器：BootstrapClassLoader --> ExtClassLoader --> AppClassLoader
Android 类加载器：BootClassLoader --> PathClassLoader --> DexClassLoader


Bitamp 占用内存大小 = 宽度像素 x （inTargetDensity / inDensity） 
                  x 高度像素 x （inTargetDensity / inDensity）
                  x 一个像素所占的内存





































