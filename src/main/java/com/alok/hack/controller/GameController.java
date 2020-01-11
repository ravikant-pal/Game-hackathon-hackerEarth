package com.alok.hack.controller;


import com.alok.hack.dao.models.AuthenticationRequest;
import com.alok.hack.dao.models.AuthenticationResponse;
import com.alok.hack.dao.models.Game;
import com.alok.hack.services.GameService;
import com.alok.hack.services.MyUserDetailsService;
import com.alok.hack.util.JwtUtil;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.io.FileReader;
import java.util.*;


@RestController
@RequestMapping("/games")
public class GameController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtTokenUtil;

    @Autowired
    MyUserDetailsService myUserDetailsService;


    @Autowired
    GameService gameService;


    @PostMapping("/authenticate")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) throws Exception {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword())
            );

        } catch (BadCredentialsException e) {

            throw new Exception("Incorrect username or password", e);
        }


        final UserDetails userDetails = myUserDetailsService
                .loadUserByUsername(authenticationRequest.getUsername());

        final String jwt = jwtTokenUtil.generateToken(userDetails);

        return ResponseEntity.ok(new AuthenticationResponse(jwt));
    }

    @GetMapping("/{id}")
    @ResponseBody
    public Game getPosts(@PathVariable Integer id) {
        return gameService.get(id).get();
    }

    @GetMapping("/all")
    @ResponseBody
    public List<Game> getAllGameBySearchOrWithoutSearch(@RequestParam(required = false, name = "keyword") String keyword) {

        if (gameService.getListOrderById().size()==0) {
            String pathToFile = "/Users/alok/IdeaProjects/hack/src/main/resources/game.csv";

            try {

                FileReader filereader = new FileReader(pathToFile);

                CSVReader csvReader = new CSVReaderBuilder(filereader)
                        .withSkipLines(1)
                        .build();
                List<String[]> allData = csvReader.readAll();
                for (String[] row : allData) {
                    gameService.save(createGame(row));
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (keyword != null) {
            return gameService.searching(keyword);
        } else {
            return gameService.getListOrderById();
        }
    }

    @PostMapping("/new-game")
    public ResponseEntity<?> newGame(
            @RequestParam(required = true, name = "title") String title,
            @RequestParam(required = false, name = "platform") String platform,
            @RequestParam(required = false, name = "score") String score,
            @RequestParam(required = false, name = "genre") String genre,
            @RequestParam(required = false, name = "editorChoice") String editorChoice) {


        Game game = new Game();
        game.setTitle(title);
        game.setPlatform(platform);
        game.setScore(score);
        game.setGenre(genre);
        game.setEditorChoice(editorChoice);


        try {
            gameService.save(game);
        } catch (Exception e) {

            return new ResponseEntity<>("Exception in saving post", HttpStatus.EXPECTATION_FAILED);

        }
        return new ResponseEntity<>("Successfully inserted", HttpStatus.OK);
    }

    @PutMapping("/update")
    public ResponseEntity<?> putGame(@RequestParam(required = true, name = "id") Integer id,
                                     @RequestParam(required = true, name = "title") String title,
                                     @RequestParam(required = false, name = "platform") String platform,
                                     @RequestParam(required = false, name = "score") String score,
                                     @RequestParam(required = false, name = "genre") String genre,
                                     @RequestParam(required = false, name = "editorChoice") String editorChoice) {

        Optional<Game> game = gameService.get(id);
        game.get().setTitle(title);
        game.get().setPlatform(platform);
        game.get().setScore(score);
        game.get().setGenre(genre);
        game.get().setEditorChoice(editorChoice);

        try {
            gameService.save(game.get());
        } catch (Exception e) {
            return new ResponseEntity<>("Exception in Update the post", HttpStatus.EXPECTATION_FAILED);
        }
        return new ResponseEntity<>("Post updated successfully", HttpStatus.OK);

    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deletePosts(@RequestParam(required = true, name = "id") Integer id) {
        try {
            gameService.delete(id);
        } catch (Exception e) {

            return new ResponseEntity<>("Exception in deleting the post", HttpStatus.EXPECTATION_FAILED);
        }
        return new ResponseEntity<>("deleted successfully", HttpStatus.OK);
    }


     static Game createGame(String[] metadata) {

        String title = metadata[0];
        String platform =metadata[1];
        String score = metadata[2];
        String genre = metadata[3];
        String editorChoice = metadata[4];
        System.out.println(title+" |"+platform+" |"+score+" |"+genre+" |"+editorChoice);

        return new Game(null, platform, title, score, genre, editorChoice);

    }
}

