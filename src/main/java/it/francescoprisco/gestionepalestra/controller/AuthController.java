package it.francescoprisco.gestionepalestra.controller;

import it.francescoprisco.gestionepalestra.dto.AuthRequest;
import it.francescoprisco.gestionepalestra.dto.JwtResponse;
import it.francescoprisco.gestionepalestra.dto.MessageResponse;
import it.francescoprisco.gestionepalestra.dto.RegisterRequest;
import it.francescoprisco.gestionepalestra.model.Cliente;
import it.francescoprisco.gestionepalestra.repository.ClienteRepository;
import it.francescoprisco.gestionepalestra.security.jwt.JwtUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    ClienteRepository clienteRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody AuthRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        User userDetails = (User) authentication.getPrincipal();
        
        List<String> roles = userDetails.getAuthorities().stream()
            .map(item -> item.getAuthority())
            .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(jwt, userDetails.getUsername(), roles));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest signUpRequest) {
        if (clienteRepository.existsByMail(signUpRequest.getMail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Errore: L'email è già in uso!"));
        }

        // Crea il nuovo account cliente
        Cliente cliente = new Cliente();
        cliente.setNome(signUpRequest.getNome());
        cliente.setCognome(signUpRequest.getCognome());
        cliente.setMail(signUpRequest.getMail());
        cliente.setPassword(encoder.encode(signUpRequest.getPassword()));
        cliente.setNfcId(signUpRequest.getNfcId());

        // Assegna il ruolo di default
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_CLIENTE"); 
        cliente.setRoles(roles);

        clienteRepository.save(cliente);

        return ResponseEntity.ok(new MessageResponse("Utente registrato con successo!"));
    }
}