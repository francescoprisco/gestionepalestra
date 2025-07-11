package it.francescoprisco.gestionepalestra.controller;

import it.francescoprisco.gestionepalestra.dto.*;
import it.francescoprisco.gestionepalestra.model.Cliente;
import it.francescoprisco.gestionepalestra.model.Receptionist;
import it.francescoprisco.gestionepalestra.repository.ClienteRepository;
import it.francescoprisco.gestionepalestra.repository.ReceptionistRepository;
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
/**
 * Controller che gestisce l'autenticazione e la registrazione degli utenti.
 * Gli endpoint qui sono pubblici.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    ClienteRepository clienteRepository;

    @Autowired
    ReceptionistRepository receptionistRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    /**
     * Autentica un utente (cliente o receptionist) e restituisce un token JWT.
     * @param loginRequest DTO con email e password.
     * @return ResponseEntity con il token JWT.
     */
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
    /**
     * Registra un nuovo utente di tipo Cliente.
     * @param signUpRequest DTO con i dati del nuovo cliente.
     * @return Messaggio di successo o errore.
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest signUpRequest) {
        if (clienteRepository.existsByEmail(signUpRequest.getEmail()) || receptionistRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Errore: L'email è già in uso!"));
        }

        Cliente cliente = new Cliente();
        cliente.setNome(signUpRequest.getNome());
        cliente.setCognome(signUpRequest.getCognome());
        cliente.setEmail(signUpRequest.getEmail());
        cliente.setPassword(encoder.encode(signUpRequest.getPassword()));
        cliente.setNfcId(signUpRequest.getNfcId());

        Set<String> roles = new HashSet<>();
        roles.add("ROLE_CLIENTE"); 
        cliente.setRoles(roles);

        clienteRepository.save(cliente);

        return ResponseEntity.ok(new MessageResponse("Utente cliente registrato con successo!"));
    }


    /**
     * Registra un nuovo utente di tipo Receptionist.
     * @param signUpRequest DTO con i dati del nuovo receptionist.
     * @return Messaggio di successo o errore.
     */
    @PostMapping("/register/receptionist")
    public ResponseEntity<?> registerReceptionist(@Valid @RequestBody ReceptionistRegisterRequest signUpRequest) {
        if (receptionistRepository.existsByEmail(signUpRequest.getEmail()) || clienteRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Errore: L'email è già in uso!"));
        }

        Receptionist receptionist = new Receptionist();
        
        receptionist.setNome(signUpRequest.getNome());
        receptionist.setCognome(signUpRequest.getCognome());
        
        receptionist.setEmail(signUpRequest.getEmail());
        receptionist.setPassword(encoder.encode(signUpRequest.getPassword()));
        
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_RECEPTIONIST");
        receptionist.setRoles(roles);
        
        receptionistRepository.save(receptionist);

        return ResponseEntity.ok(new MessageResponse("Utente receptionist registrato con successo!"));
    }
}