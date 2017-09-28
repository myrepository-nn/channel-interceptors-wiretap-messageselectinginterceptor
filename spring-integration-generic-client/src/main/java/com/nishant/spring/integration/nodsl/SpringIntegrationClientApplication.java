package com.nishant.spring.integration.nodsl;

import java.util.Scanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.interceptor.WireTap;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

import com.fasterxml.jackson.core.JsonProcessingException;
@SpringBootApplication
public class SpringIntegrationClientApplication {

	public static void main(String[] args) throws JsonProcessingException {
		ConfigurableApplicationContext cxt=SpringApplication.run(SpringIntegrationClientApplication.class, args);
		Sender sn=cxt.getBean(Sender.class);
		Scanner scn=new Scanner(System.in);
		while(scn.hasNext()) {
			String msgtosend=scn.next();
			Account act=new Account();
			act.setAccountNo("1234567");
			act.setAccountType("saving");
			if(msgtosend.equalsIgnoreCase("saving")) {
				sn.send(act);
			}else {
				sn.send("hello");	
			}
		}
		scn.close();
	}

	@MessagingGateway(defaultRequestChannel="messageChannel",errorChannel="errorChannel")
	public interface Sender{
		public void send(Object msg);
	}

	@Bean
	public MessageChannel messageChannel() {
		DirectChannel dc= new DirectChannel();
		dc.addInterceptor(new WireTap(messageOutChannel(), message -> (message.getPayload() instanceof String)?true:false));
		//uncomment it to use messageselectingchannel and comment above intercepter
		//dc.addInterceptor(new MessageSelectingInterceptor(message -> (message.getPayload() instanceof String)?true:false));
		return dc;
	}
	@Bean
	public MessageChannel errorChannel() {
		return new DirectChannel();
	}
	@Bean
	@ServiceActivator(inputChannel="errorChannel")
	public MessageHandler errorChannelhandle() {
		return new MessageHandler() {

			@Override
			public void handleMessage(Message<?> message) throws MessagingException {
				System.out.println("logger errorChannel="+message);

			}
		};

	}
	@Bean
	public MessageChannel messageOutChannel() {
		return new DirectChannel();
	}
	@Bean
	@ServiceActivator(inputChannel="messageOutChannel")
	public MessageHandler messageOutChannelhandle() {
		return new MessageHandler() {

			@Override
			public void handleMessage(Message<?> message) throws MessagingException {
				System.out.println("logger messageOutChannel="+message);

			}
		};

	}
	@Bean
	@ServiceActivator(inputChannel="messageChannel")
	public MessageHandler messageChannelhandle() {
		return new MessageHandler() {

			@Override
			public void handleMessage(Message<?> message) throws MessagingException {
				System.out.println("messageChannel="+message);

			}
		};

	}

}
