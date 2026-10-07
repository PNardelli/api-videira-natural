package com.api.repository;

import com.api.entity.Parceiros;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParceirosRepository extends JpaRepository<Parceiros, Long> {
}
