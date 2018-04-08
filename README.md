## 基于Netty实现的轻量级RPC框架


### rpc-server
1. 框架的rpc服务端，通过@RpcServer注解将用户系统业务类发布为rpc服务
2. 定义RpcHandler，接受请求，调用对应接口方法，返回结果

### rpc-client
1. 框架的rpc客户端，用于发送rpc请求（封装为request对象）
2. RpcProxy用于创建rpc代理服务

### rpc-common
1. RpcDecoder：rpc解码器
2. RpcEncoder：rpc编码器
3. RpcRequest、RpcResponse：封装rpc请求和响应对象
4. SerializationUtil：基于ProtoStuff实现的序列化工具类

### rpc-registry
1. ServiceRegistry：用于服务启动时向zookeeper注册节点
2. ServiceDiscovery：用于发现可用的rpc服务节点和监测服务节点的动态上下线

### rpc-sample-interface
1. 接口对象类

### rpc-sample-server
1. rpc服务端测试工程，通过@RpcService将定义的类发布为rpc服务
```
@RpcService(IHelloService.class)
public class HelloServiceImpl implements IHelloService {

    public String hello(String name) {
        System.out.println("已经调用服务端接口实现，业务处理结果为：" + "Hello! " + name);
        return "Hello! " + name;
    }

    public String hello(Person person) {
        System.out.println("已经调用服务端接口实现，业务处理结果为：" + "Hello! " + person.getName() + " " + person.getAge());
        return "Hello! " + person.getName() + " " + person.getAge();
    }
}
```
2. 在spring.xml中配置bean
```
<bean id="serviceRegistry" class="com.chris.rpc.registry.ServiceRegistry">
    <constructor-arg name="registryAddress" value="${registry.address}"/>
</bean>

<bean id="rpcServer" class="com.chris.rpc.server.RpcServer">
    <constructor-arg name="serverAddress" value="${server.address}"/>
    <constructor-arg name="serviceRegistry" ref="serviceRegistry"/>
</bean>
```

### rpc-sample-app
1. rpc客户端测试工程，通过RpcProxy创建代理
```
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring.xml")
public class HelloServiceTest implements ApplicationContextAware {

    @Autowired
    private RpcProxy rpcProxy;

    @Test
    public void helloString() {
        //调用代理的crate方法，代理HelloService接口
        IHelloService helloService = rpcProxy.create(IHelloService.class);
        //调用代理的方法，执行invoke
        String result = helloService.hello("Chris");
        System.out.println("服务端返回结果为：" + result);
    }

    @Test
    public void helloObject() {
        //调用代理的crate方法，代理HelloService接口
        IHelloService helloService = rpcProxy.create(IHelloService.class);
        //调用代理的方法，执行invoke
        String result = helloService.hello(new Person("Alan", "27"));
        System.out.println("服务端返回结果为：" + result);
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

    }
}
```
2. 在spring.xml中配置
```
<bean id="serviceDiscovery" class="com.chris.rpc.registry.ServiceDiscovery">
    <constructor-arg name="registryAddress" value="${registry.address}"/>
</bean>

<bean id="rpcProxy" class="com.chris.rpc.client.RpcProxy">
    <constructor-arg name="serviceDiscovery" ref="serviceDiscovery"/>
</bean>
```

## TODO LIST
1. 设置RPC服务的负载均衡策略，或使用轮询访问
2. 扩展多协议传输
3. 管理控制台与简单的控制中心