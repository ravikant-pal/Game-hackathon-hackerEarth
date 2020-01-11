package com.alok.hack.services;



import com.alok.hack.dao.models.Game;
import com.alok.hack.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class GameService {
    @Autowired
    GameRepository repo;

    public void save(Game game) {
        repo.save(game);
    }
    public List<Game> getListOrderById() {
        return repo.findAllByOrderByIdAsc();
    }

    public List<Game> searching(String keyword) {
        return repo.findAllByTitleContainsOrPlatformContainsOrScoreContainsOrGenreContainsOrderById(keyword,keyword,keyword,keyword);
    }

    public Optional<Game> get(Integer id) {
        return repo.findById(id);
    }

    public void delete(Integer id) {
        repo.deleteById(id);
    }
}
