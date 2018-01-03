package com.abin.lee.spring.rabbitmq.stub.test;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

import java.io.IOException;

/**
 * Created by abin on 2018/1/3 14:47.
 * spring-rabbitmq1
 * com.abin.lee.spring.rabbitmq.stub.test
 */
public class ProducerTest {
    public static void main(String[] args) {
        String exchangeName = "lend.exchange";
        String queueName = "lend.queue";
        String routingKey = "lend.queue";
        String bindingKey = "lend.queue";
        int count = 3;

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("172.16.2.146");
        factory.setUsername("guest");
        factory.setPassword("guest");
        factory.setPort(5672);

        //创建生产者
        Sender producer = new Sender(factory, count, exchangeName, queueName,routingKey,bindingKey);
        producer.run();
    }
}

class Sender
{
    private ConnectionFactory factory;
    private int count;
    private String exchangeName;
    private String 	queueName;
    private String routingKey;
    private String bindingKey;

    public Sender(ConnectionFactory factory, int count, String exchangeName, String queueName, String routingKey, String bindingKey) {
        this.factory = factory;
        this.count = count;
        this.exchangeName = exchangeName;
        this.queueName = queueName;
        this.routingKey = routingKey;
        this.bindingKey = bindingKey;
    }

    public void run() {
        Channel channel = null;
        try {
            Connection connection = factory.newConnection();
            channel = connection.createChannel();
            //创建exchange
            channel.exchangeDeclare(exchangeName, "direct", true, false, null);
            //创建队列
            channel.queueDeclare(queueName, true, false, false, null);
            //绑定exchange和queue
            channel.queueBind(queueName, exchangeName, bindingKey);
            //发送持久化消息
            for(int i = 0;i < count;i++)
            {
                //第一个参数是exchangeName(默认情况下代理服务器端是存在一个""名字的exchange的,
                //因此如果不创建exchange的话我们可以直接将该参数设置成"",如果创建了exchange的话
                //我们需要将该参数设置成创建的exchange的名字),第二个参数是路由键
                //开启事务
                channel.txSelect();
                channel.basicPublish(exchangeName, routingKey, true, MessageProperties.PERSISTENT_BASIC, ("第"+(i+1)+"条消息").getBytes());
                if(i == 1)
                {
                    int result = 1/0;
                }
                //提交事务
                channel.txCommit();
            }
        } catch (Exception e) {
            try {
                //回滚操作
                channel.txRollback();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
    }
}