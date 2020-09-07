# utils

## chan 

java实现go channel功能, eg: [样例](src/main/java/com/github/zhanglp92/example/ChanExample.java)

```java
public class Sample {
        public void doThing() {
            // 初始化带缓冲区和不带缓冲区的chan
            Chan<Integer> ch = new Chan<>();
            Chan<Integer> ch = new Chan<>(40);

            // 写数据(支持批量迭代)
            ch.send(val);

            // 读取单条数据, 且阻塞
            log.info("读取chan阻塞, data = {}", ch.recv());

            // 遍历读取, 且阻塞
            ch.forEach(node -> System.out.printf("A -> %s\n", node));

            // 关闭. 写异常, 读正常(默认返回null)
            ch.close();
        }
}
```