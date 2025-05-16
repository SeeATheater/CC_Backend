package cc.backend.repository;

import cc.backend.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public class BoardRepository extends JpaRepository<Board, Long> {
}
