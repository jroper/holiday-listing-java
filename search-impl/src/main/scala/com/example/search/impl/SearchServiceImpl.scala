package com.example.search.impl

import java.io.{File, FileInputStream, FileOutputStream}
import java.time.LocalDate
import java.util.UUID

import akka.Done
import akka.actor.{Actor, ActorSystem, Props, Status}
import com.example.search.api.{ListingSearchResult, SearchService}
import com.example.reservation.api.{ReservationAdded, ReservationService}
import akka.pattern.ask
import akka.stream.scaladsl.Flow
import akka.util.Timeout
import com.lightbend.lagom.javadsl.api.transport.NotFound
import javax.inject.{Inject, Singleton}
import play.libs.Json

import scala.concurrent.duration._
import scala.compat.java8.FutureConverters._
import scala.collection.JavaConverters._
import java.util

import com.lightbend.lagom.javadsl.api.ServiceCall
import org.pcollections.HashTreePSet

import scala.beans.BeanProperty

/**
  * Implementation of the SearchService.
  */
@Singleton
class SearchServiceImpl @Inject() (reservationService: ReservationService, actorSystem: ActorSystem) extends SearchService {

  import SearchActor._
  private val searchActor = actorSystem.actorOf(Props[SearchActor])
  implicit val searchActorTimeout = Timeout(10.seconds)
  
  override def searchListings(checkin: LocalDate, checkout: LocalDate) = { _ =>
    (searchActor ? Search(checkin, checkout)).mapTo[util.List[ListingSearchResult]].toJava
  }

  override def listingName(listingId: UUID) = { _ =>
    (searchActor ? ListingName(listingId)).mapTo[String].toJava
  }

  override def reservationAdded: ServiceCall[ReservationAdded, Done] = { reservation =>
    (searchActor ? reservation).mapTo[Done].toJava
  }
}

private object SearchActor {
  case class Search(checkin: LocalDate, checkout: LocalDate)
  case class ListingName(listingId: UUID)
}

private class SearchActor extends Actor {
  import SearchActor._

  val repo = new SearchRepository

  override def receive = {

    case reservation: ReservationAdded =>
      sender() ! repo.add(reservation)

    case Search(checkin, checkout) =>
      sender() ! repo.search(checkin, checkout)

    case ListingName(listingId) =>
      repo.name(listingId) match {
        case Some(name) => sender() ! name
        case None => sender() ! Status.Failure(new NotFound(s"Listing $listingId not found"))
      }
  }
}

/**
  * Not at all an efficient index, but this is a demo and this code isn't the subject of the demo
  */
private class SearchRepository {
  private val reservationFile = new File("./target/search-index.json")

  private var reservations: Map[UUID, ListingIndex] = if (reservationFile.exists()) {
    val is = new FileInputStream(reservationFile)
    try {
      val raw = Json.fromJson(Json.parse(is), classOf[ListingIndexMap]).map.asScala
      raw.map {
        case (id, index) => UUID.fromString(id) -> index
      }.toMap
    } finally {
      is.close()
    }
  } else {
    Seq(
      ListingSearchResult(UUID.randomUUID(), "Beach house with wonderful views", "beachhouse.jpeg", 280),
      ListingSearchResult(UUID.randomUUID(), "Villa by the water", "villa.jpeg", 350),
      ListingSearchResult(UUID.randomUUID(), "Budget hotel convenient to town centre", "hotel.jpeg", 120),
      ListingSearchResult(UUID.randomUUID(), "Quaint country B&B", "bnb.jpeg", 180)
    ).map { listing =>
      listing.listingId -> ListingIndex(listing, HashTreePSet.empty())
    }.toMap
  }

  if (!reservationFile.exists()) {
    writeOut()
  }

  private def writeOut(): Unit = {
    val json = Json.stringify(Json.toJson(ListingIndexMap(reservations.map {
      case (id, index) => id.toString -> index
    }.asJava)))
    val os = new FileOutputStream(reservationFile)
    try {
      os.write(json.getBytes("utf-8"))
      os.flush()
    } finally {
      os.close()
    }
  }

  def add(reservation: ReservationAdded): Done = {
    reservations.get(reservation.getListingId) match {
      case Some(ListingIndex(listing, res)) =>
        if (res.asScala.forall(_.getReservationId != reservation.getReservationId)) {
          reservations += (listing.listingId -> ListingIndex(listing, HashTreePSet.from(res).plus(reservation)))
          writeOut()
        }
        Done
      case None =>
        // Ignore
        Done
    }
  }

  def search(checkin: LocalDate, checkout: LocalDate): util.List[ListingSearchResult] = {
    reservations.values.collect {
      case ListingIndex(listing, res) if res.asScala.forall(reservationDoesNotConflict(checkin, checkout)) => listing
    }.toList.asJava
  }

  def name(listingId: UUID): Option[String] = {
    reservations.get(listingId).map(_.listing.listingName)
  }

  private def reservationDoesNotConflict(checkin: LocalDate, checkout: LocalDate)(reservationAdded: ReservationAdded): Boolean = {
    val rCheckin = reservationAdded.getReservation.getCheckin
    val rCheckout = reservationAdded.getReservation.getCheckout

    if (checkout.isBefore(rCheckin) || checkout == rCheckin) {
      true
    } else if (checkin.isAfter(rCheckout) || checkin == rCheckout) {
      true
    } else {
      false
    }
  }
}

private case class ListingIndex(@BeanProperty var listing: ListingSearchResult, @BeanProperty var reservations: util.Set[ReservationAdded]) {
  def this() = this(null, null)
}
private case class ListingIndexMap(@BeanProperty var map: util.Map[String, ListingIndex]) {
  def this() = this(null)
}