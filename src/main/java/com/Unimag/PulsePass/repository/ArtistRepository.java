package com.Unimag.PulsePass.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Unimag.PulsePass.domain.Artist;

public interface ArtistRepository extends JpaRepository<Artist, Long> {

    Optional<Artist> findByStageName(String stageName);

}