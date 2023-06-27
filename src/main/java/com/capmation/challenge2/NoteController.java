package com.capmation.challenge2;

import java.net.URI;
import java.security.Principal;
import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/notes")
public class NoteController {
	
	private NoteRepository noteRepository;
	private UserDetailsService userDetailsService;

	public NoteController(NoteRepository noteRepository, UserDetailsService userDetailsService) {
		this.noteRepository = noteRepository;
		this.userDetailsService = userDetailsService;
	}
	
	/**
	 * Retrieves specific note by Id
	 * @param requestedId
	 * @param principal
	 * @return @see {@link com.capmation.challenge2.Note}
	 */
	@GetMapping("/{requestedId}")
    public ResponseEntity<Note> findById(@PathVariable Long requestedId, Principal principal) {
		Note note = findNote(requestedId, principal);
        if (note != null) {
            return ResponseEntity.ok(note);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
	
	/**
	 * Retrieves all user notes with pagination
	 * @param pageable
	 * @param principal
	 * @return @see {@link java.util.List} --> {@link com.capmation.challenge2.Note}
	 */
	@GetMapping
    public ResponseEntity<List<Note>> findAll(Pageable pageable, Principal principal) {
        Page<Note> page = findNotes(pageable, principal);
        return ResponseEntity.ok(page.getContent());
    }
	
	/**
	 * Creates a new user Note
	 * @param newNoteRequest
	 * @param ucb
	 * @param principal
	 * @return @see {@link java.lang.Void}
	 */
	@PostMapping
	private ResponseEntity<Void> createNote(@RequestBody Note newNoteRequest, UriComponentsBuilder ucb, Principal principal){
		/*
		 * TODO
		 * You need to:
		 * 1. Validate that the Title and Body properties are present and have a valid value, if not valid values found then return a bad request 400 response message 
		 * 2. Create a new Note object including the dynamic properties (owner, createdOn, modifiedOn)
		 * 3. Using the UriComponentsBuilder return the path of the created note in the response header
		 * 4. Return a created 201 HTTP response message with the new URI
		 */
		if(newNoteRequest.getTitle() == null 
				|| newNoteRequest.getTitle().isBlank() 
				|| newNoteRequest.getBody() == null 
				|| newNoteRequest.getBody().isBlank()) {
			return ResponseEntity.badRequest().build();
		}
		Note savedNote = noteRepository.save(new Note(null, newNoteRequest.getTitle(), newNoteRequest.getBody(), new Date(), new Date(), principal.getName()));
		URI locationOfNewNote = ucb
                .path("notes/{id}")
                .buildAndExpand(savedNote.getId())
                .toUri();
        return ResponseEntity.created(locationOfNewNote).build();
	}
	
	/**
	 * Updates existing user Note
	 * @param requestedId
	 * @param updateNoteRequest
	 * @param principal
	 * @return @see {@link java.lang.Void}
	 */
	@PutMapping("/{requestedId}")
	private ResponseEntity<Void> updateNote(@PathVariable Long requestedId, @RequestBody Note updateNoteRequest, Principal principal){
		/*
		 * TODO
		 * You need to:
		 * 1. Validate if note exists and is owned by the user making the request
		 * 2. Be sure to validate there was a change in the note before updating it (The only able fields to be updated are Title and Body)
		 * 2. If the previous validation is passed then update the note accordingly and return a 204 HTTP response message (no content),
		 *    otherwise, return the corresponding 404 not found HTTP response message.
		 */
		Note note = findNote(requestedId, principal);
        if (note != null) {
        	boolean changed = false;
            if(updateNoteRequest.getTitle() != null 
            		&& !updateNoteRequest.getTitle().isBlank() 
            		&& !updateNoteRequest.getTitle().equals(note.getTitle())) {
            	note.setTitle(updateNoteRequest.getTitle());
            	changed = true;
            }
            if(updateNoteRequest.getBody() != null 
            		&& !updateNoteRequest.getBody().isBlank()
            		&& !updateNoteRequest.getBody().equals(note.getBody())) {
            	note.setBody(updateNoteRequest.getBody());
            	changed = true;
            }
            if(changed) {
            	note.setModifiedOn(new Date());
            	noteRepository.save(note);
            }
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
	}
	
	/**
	 * Finds an user note by Id and active session
	 * @param requestedId
	 * @param principal
	 * @return @see {@link com.capmation.challenge2.Note}
	 */
	private Note findNote(Long requestedId, Principal principal) {
		UserDetails details = userDetailsService.loadUserByUsername(principal.getName());
		if (details != null && details.getAuthorities().stream()
			      .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
			return noteRepository.findById(requestedId).get();
		}
        return noteRepository.findByIdAndOwner(requestedId, principal.getName());
    }
	
	/**
	 * Finds all user notes by active session and provided pagination
	 * @param pageable
	 * @param principal
	 * @return @see {@link org.springframework.data.domain.Page} --> {@link com.capmation.challenge2.Note}
	 */
	private Page<Note> findNotes(Pageable pageable, Principal principal) {
		UserDetails details = userDetailsService.loadUserByUsername(principal.getName());
		System.out.println("USER DETAILS: " + details.toString());
		if (details != null && details.getAuthorities().stream()
			      .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
			return noteRepository.findAll(
					PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    pageable.getSortOr(Sort.by(Sort.Direction.DESC, "createdOn"))
            ));
		}
        return noteRepository.findByOwner(principal.getName(),
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.DESC, "createdOn"))
                ));
    }
	
}
