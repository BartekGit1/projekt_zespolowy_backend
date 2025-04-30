package pl.lodz.p.zesp.watchlist;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WatchlistRepository extends JpaRepository<WatchlistEntity, Long> {
    Optional<WatchlistEntity> findByAuction_IdAndUser_username(Long auctionId, String username);

    @Query("SELECT w FROM WatchlistEntity w JOIN FETCH w.user u JOIN FETCH w.auction WHERE u.username=:username")
    List<WatchlistEntity> findAllWatchedAuctionsByUser(@Param(value = "username") String username);
}
