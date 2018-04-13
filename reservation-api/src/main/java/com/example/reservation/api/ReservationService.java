package com.example.reservation.api;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;

import static com.lightbend.lagom.javadsl.api.Service.*;

import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.api.broker.kafka.KafkaProperties;
import com.lightbend.lagom.javadsl.api.transport.Method;

import java.util.List;
import java.util.UUID;

public interface ReservationService extends Service {

  String TOPIC_NAME = "reservations";

  ServiceCall<Reservation, ReservationAdded> reserve(UUID listingId);

  ServiceCall<NotUsed, List<Reservation>> getCurrentReservations(UUID listingId);

  Topic<ReservationAdded> reservationEvents();

  @Override
  default Descriptor descriptor() {
    // @formatter:off
    return named("reservation")
        .withCalls(
            restCall(Method.POST, "/api/listing/:id/reservation", this::reserve),
            restCall(Method.GET, "/api/listing/:id/reservations", this::getCurrentReservations)
        )
        .withTopics(
            topic(TOPIC_NAME, this::reservationEvents)
                .withProperty(
                    KafkaProperties.partitionKeyStrategy(),
                    event -> event.getListingId().toString()
                )
        )
        .withAutoAcl(true);
  }
  // @formatter:on
}
