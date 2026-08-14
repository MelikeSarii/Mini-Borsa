package com.miniborsa.orderservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import quickfix.DefaultMessageFactory;
import quickfix.FileLogFactory;
import quickfix.FileStoreFactory;
import quickfix.Initiator;
import quickfix.SessionSettings;
import quickfix.SocketInitiator;
import org.springframework.context.ConfigurableApplicationContext;
import com.miniborsa.orderservice.fix.FixApplication;

@SpringBootApplication
public class OrderServiceApplication {

	public static void main(String[] args) throws Exception {

		// Spring Boot'u başlat
		ConfigurableApplicationContext context =
				SpringApplication.run(OrderServiceApplication.class, args);

		// FIX ayarlarını oku
		SessionSettings settings =
				new SessionSettings("src/main/resources/initiator.cfg");

		// Spring'in oluşturduğu FixApplication bean'ini al
		FixApplication application =
				context.getBean(FixApplication.class);

		// FIX mesajlarının saklanması
		FileStoreFactory storeFactory =
				new FileStoreFactory(settings);

		// FIX logları
		FileLogFactory logFactory =
				new FileLogFactory(settings);

		// FIX mesajlarını oluşturacak yapı
		DefaultMessageFactory messageFactory =
				new DefaultMessageFactory();

		// Order Service'i FIX Initiator olarak oluştur
		Initiator initiator = new SocketInitiator(
				application,
				storeFactory,
				settings,
				logFactory,
				messageFactory
		);

		// FIX bağlantısını başlat
		initiator.start();

		System.out.println("FIX Initiator başlatıldı!");
		System.out.println("Matching Engine'e bağlantı bekleniyor...");
	}
}