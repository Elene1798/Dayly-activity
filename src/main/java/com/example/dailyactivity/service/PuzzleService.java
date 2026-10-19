package com.example.dailyactivity.service;

import com.example.dailyactivity.model.MiniGame;
import com.example.dailyactivity.model.Puzzle;
import com.example.dailyactivity.repository.PuzzleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class PuzzleService {

    private final PuzzleRepository puzzleRepository;
    private final FileStorageService fileStorage;

    public PuzzleService(
            PuzzleRepository puzzleRepository,
            FileStorageService fileStorage
    ) {
        this.puzzleRepository = puzzleRepository;
        this.fileStorage = fileStorage;
    }

    public List<Puzzle> getByMiniGame(MiniGame miniGame) {
        return puzzleRepository.findByMiniGame(miniGame);
    }

    public List<Puzzle> getActiveByMiniGame(MiniGame miniGame) {
        return puzzleRepository.findByMiniGameAndActiveTrue(miniGame);
    }

    public Puzzle findById(Long id) {
        return puzzleRepository.findById(id).orElse(null);
    }

    @Transactional
    public Puzzle save(Puzzle puzzle, MultipartFile image) {

        if (image != null && !image.isEmpty()) {
            String fileName = fileStorage.save(image, "puzzles");
            puzzle.setImageFile(fileName);
        }

        return puzzleRepository.save(puzzle);
    }

    @Transactional
    public void delete(Long id) {
        Puzzle puzzle = puzzleRepository.findById(id).orElse(null);
        if (puzzle == null) return;

        fileStorage.delete("puzzles", puzzle.getImageFile());
        puzzleRepository.delete(puzzle);
    }
}