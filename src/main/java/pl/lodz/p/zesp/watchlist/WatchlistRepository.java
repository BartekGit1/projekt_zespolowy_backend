package pl.lodz.p.zesp.watchlist;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WatchlistRepository extends JpaRepository<WatchlistEntity, Long> {
}
