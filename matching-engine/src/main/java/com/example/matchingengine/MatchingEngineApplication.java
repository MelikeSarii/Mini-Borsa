package com.example.matchingengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import com.example.matchingengine.engine.MatchingEngine;
import quickfix.Acceptor;
import quickfix.SessionSettings;
import quickfix.FileStoreFactory;
import quickfix.FileLogFactory;
import quickfix.DefaultMessageFactory;
import quickfix.SocketAcceptor;

import com.example.matchingengine.fix.FixApplication;

@SpringBootApplication
@EnableScheduling
public class MatchingEngineApplication {

    public static void main(String[] args) throws Exception {

        SpringApplication.run(MatchingEngineApplication.class, args);

        // FIX ayarlarını oku
        SessionSettings settings =
                new SessionSettings("src/main/resources/acceptor.cfg");

        // FIX uygulamamız
        MatchingEngine matchingEngine = new MatchingEngine();

        FixApplication application = new FixApplication(matchingEngine);

        // Mesajların saklanması
        FileStoreFactory storeFactory =
                new FileStoreFactory(settings);

        // FIX logları
        FileLogFactory logFactory =
                new FileLogFactory(settings);

        // FIX mesajlarının oluşturulması
        DefaultMessageFactory messageFactory =
                new DefaultMessageFactory();

        // FIX Acceptor'ı oluştur
        Acceptor acceptor = new SocketAcceptor(
                application,
                storeFactory,
                settings,
                logFactory,
                messageFactory
        );

        // FIX motorunu başlat
        acceptor.start();

        System.out.println("FIX Acceptor başlatıldı!");
        System.out.println("FIX bağlantısı bekleniyor...");
    }
}