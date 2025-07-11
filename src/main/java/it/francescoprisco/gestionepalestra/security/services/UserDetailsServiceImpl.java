package it.francescoprisco.gestionepalestra.security.services;

import it.francescoprisco.gestionepalestra.model.Cliente;
import it.francescoprisco.gestionepalestra.model.Receptionist;
import it.francescoprisco.gestionepalestra.repository.ClienteRepository;
import it.francescoprisco.gestionepalestra.repository.ReceptionistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ReceptionistRepository receptionistRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // First, try to find a Cliente
        var clienteOpt = clienteRepository.findByEmail(username);
        if (clienteOpt.isPresent()) {
            Cliente cliente = clienteOpt.get();
            return new User(
                cliente.getEmail(),
                cliente.getPassword(),
                cliente.getRoles().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList())
            );
        }

        // If not a Cliente, try to find a Receptionist
        var receptionistOpt = receptionistRepository.findByEmail(username);
        if (receptionistOpt.isPresent()) {
            Receptionist receptionist = receptionistOpt.get();
            return new User(
                receptionist.getEmail(),
                receptionist.getPassword(),
                receptionist.getRoles().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList())
            );
        }

        throw new UsernameNotFoundException("User Not Found with email: " + username);
    }
}