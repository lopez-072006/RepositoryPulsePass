package com.Unimag.PulsePass.repository;

import com.Unimag.PulsePass.domain.Artist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtistRepository extends JpaRepository<Artist, Long> {

}