package com.example.search.api

import java.time.LocalDate
import java.util.UUID

import akka.{Done, NotUsed}
import com.lightbend.lagom.javadsl.api.deser.PathParamSerializers
import com.lightbend.lagom.javadsl.api.transport.Method
import com.lightbend.lagom.javadsl.api.{Service, ServiceCall}
import java.util

import com.example.reservation.api.ReservationAdded

import scala.beans.BeanProperty

/**
  * The search service interface.
  *
  * This describes everything that Lagom needs to know about how to serve and
  * consume the search service service.
  */
trait SearchService extends Service {

  def searchListings(checkin: LocalDate, checkout: LocalDate): ServiceCall[NotUsed, util.List[ListingSearchResult]]
  def listingName(listingId: UUID): ServiceCall[NotUsed, String]
  def reservationAdded(): ServiceCall[ReservationAdded, Done]

  override final def descriptor = {
    import com.lightbend.lagom.javadsl.api.ScalaService._

    named("search")
      .withCalls(
        restCall(Method.GET, "/api/search?checkin&checkout", searchListings _),
        restCall(Method.GET, "/api/listing/:listingId/name", listingName _),
        restCall(Method.PUT, "/api/reservations/added", reservationAdded _)
      ).withAutoAcl(true)
      .withPathParamSerializer(classOf[LocalDate],
        PathParamSerializers.required[LocalDate]("LocalDate", LocalDate.parse(_), _.toString))
  }
}

case class ListingSearchResult(@BeanProperty var listingId: UUID, @BeanProperty var listingName: String, @BeanProperty var image: String, @BeanProperty var price: Int) {
  def this() = this(null, null, null, 0)
}
