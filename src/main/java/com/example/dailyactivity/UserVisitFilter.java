
package com.example.dailyactivity;

import com.example.dailyactivity.model.User;
import com.example.dailyactivity.repository.UserRepository;
import com.example.dailyactivity.service.AchievementCheckerService;
import com.example.dailyactivity.service.UserVisitService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class UserVisitFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final UserVisitService userVisitService;
    private final AchievementCheckerService achievementCheckerService;

    public UserVisitFilter(
            UserRepository userRepository,
            UserVisitService userVisitService,
            AchievementCheckerService achievementCheckerService) {

        this.userRepository = userRepository;
        this.userVisitService = userVisitService;
        this.achievementCheckerService = achievementCheckerService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        return path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/")
                || path.startsWith("/webjars/")
                || path.equals("/favicon.ico");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {

            userRepository.findByUsername(authentication.getName())
                    .ifPresent((User user) -> {

                        boolean newVisit =
                                userVisitService.recordVisit(user);

                        if (newVisit) {
                            achievementCheckerService
                                    .checkVisitAchievements(user);
                        }
                    });
        }

        filterChain.doFilter(request, response);
    }
}
