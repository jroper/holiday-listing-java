package com.example.reservation.impl;

import akka.Done;
import akka.actor.ActorSystem;
import akka.kafka.ProducerSettings;
import akka.kafka.javadsl.Producer;
import akka.stream.Materializer;
import akka.stream.javadsl.Source;
import com.example.reservation.api.ReservationAdded;
import com.example.reservation.api.ReservationService;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import play.libs.Json;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.CompletionStage;

@Singleton
public class EventPublisher {

  private final ActorSystem system;
  private final Materializer materializer;
  private final ProducerSettings<String, String> producerSettings;

  @Inject
  public EventPublisher(ActorSystem system, Materializer materializer) {
    this.system = system;
    this.materializer = materializer;

    this.producerSettings = ProducerSettings.create(this.system, new StringSerializer(), new StringSerializer())
        .withBootstrapServers(this.system.settings().config().getString("lagom.broker.kafka.brokers"));
  }

  public CompletionStage<Done> publishEvent(ReservationAdded reservation) {
    return Source.single(reservation)
            .map(elem -> new ProducerRecord<>(ReservationService.TOPIC_NAME, elem.getListingId().toString(), Json.stringify(Json.toJson(elem))))
            .runWith(Producer.plainSink(producerSettings), materializer);
  }
}
