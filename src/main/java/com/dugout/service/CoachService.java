package com.dugout.service;

import com.dugout.model.Coach;
import com.dugout.repository.CoachRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CoachService implements UserDetailsService {

    private final CoachRepository coachRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Coach coach = coachRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Coach not found: " + username));

        return new User(
                coach.getUsername(),
                coach.getPassword(),
                coach.isEnabled(),
                true, true, true,
                List.of(new SimpleGrantedAuthority("ROLE_" + coach.getRole().name()))
        );
    }

    public Coach getCurrentAuthenticatedCoach() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return coachRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Coach not found"));
    }

    public Coach findByUsername(String username) {
        return coachRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Coach not found: " + username));
    }
}
