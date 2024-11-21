package com.example.ug_project.controller;

import com.example.ug_project.UgProjectApplication;
import com.example.ug_project.model.SongData;
import com.example.ug_project.repos.ClusterRepo;
import com.example.ug_project.service.Search;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class MainController {

    @Autowired
    ClusterRepo clusterRepo;

    @GetMapping("/")
    public String home(HttpServletRequest request, Model model) {
        return "home";
    }

    @GetMapping("/search")
    public String search(HttpServletRequest request, Model model, Principal principal) {

        String filename = "C:\\Users\\joshu\\Documents\\University\\lmd\\lmd_full\\0\\00cf795326b199f5389717e61aa3d2d5.mid\\";
        List<SongData> searchResults = Search.similarSongs(clusterRepo, filename);
        String result = "";
        for (SongData i : searchResults) {
            result += i.toString() + "\n\n";
        }
        model.addAttribute("searchResults", searchResults);
        return "search";
    }


    @PostMapping(value="/search")
    public String fileUpload(@ModelAttribute("file") String thefile, @RequestParam("file") File file, Model model) {
        String searchResults = "";
        model.addAttribute(thefile);
        model.addAttribute("searchResults", searchResults);

        return "/searchResults";
    }
}
