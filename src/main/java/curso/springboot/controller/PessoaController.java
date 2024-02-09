package curso.springboot.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import curso.springboot.repository.ProfissaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import curso.springboot.model.Pessoa;
import curso.springboot.model.Telefone;
import curso.springboot.repository.PessoaRepository;
import curso.springboot.repository.TelefoneRepository;

@Controller
public class PessoaController {
	
	@Autowired
	private PessoaRepository pessoaRepository;
	
	@Autowired
	private TelefoneRepository telefoneRepository;

	@Autowired
	private ProfissaoRepository profissaoRepository;
	
	@GetMapping("/cadastroPessoa")
	public ModelAndView inicio() {
		ModelAndView modelAndView = new ModelAndView("cadastro/cadastroPessoa");
		modelAndView.addObject("pessoaObj", new Pessoa());
		Iterable<Pessoa> pessoasIt = pessoaRepository.findAll();
		modelAndView.addObject("pessoas", pessoasIt);
		modelAndView.addObject("profissoes", profissaoRepository.findAll());
		return modelAndView;
	}
	
	@PostMapping(value = "/salvarPessoa", consumes = {"multipart/form-data"})
	public ModelAndView salvar(@Valid Pessoa pessoa, BindingResult bindingResult, final MultipartFile file) throws IOException {

		pessoa.setTelefones(telefoneRepository.getTelefones(pessoa.getId()));
		
		if(bindingResult.hasErrors()) {
			ModelAndView modelAndView = new ModelAndView("cadastro/cadastroPessoa");
			Iterable<Pessoa> pessoasIt = pessoaRepository.findAll();
			modelAndView.addObject("pessoas", pessoasIt);
			modelAndView.addObject("pessoaObj", pessoa);
			
			List<String> msg = new ArrayList<String>();
			for(ObjectError objectError : bindingResult.getAllErrors()) {
				msg.add(objectError.getDefaultMessage());
			}
			
			modelAndView.addObject("msg", msg);
			modelAndView.addObject("profissoes", profissaoRepository.findAll());
			return modelAndView;
		}

		if(file.getSize() > 0){
			pessoa.setCurriculo(file.getBytes());
			pessoa.setTipoFileCurriculo(file.getContentType());
			pessoa.setNomeFileCurriculo(file.getOriginalFilename());
		} else {
			if(pessoa.getId() != null && pessoa.getId() > 0){
				Pessoa pessoaTemp = pessoaRepository.findById(pessoa.getId()).get();
				pessoa.setCurriculo(pessoaTemp.getCurriculo());
				pessoa.setTipoFileCurriculo(pessoaTemp.getTipoFileCurriculo());
				pessoa.setNomeFileCurriculo(pessoaTemp.getNomeFileCurriculo());
			}
		}
		
		pessoaRepository.save(pessoa);
		
		ModelAndView andView = new ModelAndView("cadastro/cadastroPessoa");
		Iterable<Pessoa> pessoasIt = pessoaRepository.findAll();
		andView.addObject("pessoas", pessoasIt);
		andView.addObject("pessoaObj", new Pessoa());
		return andView;
	}
	
	@GetMapping("/listaPessoas")
	public ModelAndView pessoas() {
		ModelAndView andView = new ModelAndView("cadastro/cadastroPessoa");
		Iterable<Pessoa> pessoasIt = pessoaRepository.findAll();
		andView.addObject("pessoas", pessoasIt);
		andView.addObject("pessoaObj", new Pessoa());
		return andView;
	}

//	@GetMapping("/editarPessoa/{idPessoa}")
//	public ModelAndView editar(@PathVariable("idPessoa") Long idPessoa) {
//
//		Optional<Pessoa> pessoa = pessoaRepository.findById(idPessoa);
//
//		ModelAndView modelAndView = new ModelAndView("cadastro/cadastroPessoa");
//		modelAndView.addObject("pessoaObj", pessoa.get());
//		modelAndView.addObject("profissoes", profissaoRepository.findAll());
//		return modelAndView;
//	}

	@GetMapping("/editarPessoa/{idPessoa}")
	public ModelAndView editar(@PathVariable("idPessoa") Long idPessoa) {

		Optional<Pessoa> pessoa = pessoaRepository.findById(idPessoa);

		if (pessoa.isPresent()) {
			ModelAndView modelAndView = new ModelAndView("cadastro/cadastroPessoa");
			modelAndView.addObject("pessoaObj", pessoa.get());
			modelAndView.addObject("profissoes", profissaoRepository.findAll());
			return modelAndView;
		} else {
			// Retorna uma página de erro ou lança uma exceção, conforme sua preferência
			return new ModelAndView("error/404");
		}
	}


	@GetMapping("/removerPessoa/{idPessoa}")
	public ModelAndView excluir(@PathVariable("idPessoa") Long idPessoa) {
		
		pessoaRepository.deleteById(idPessoa);
		
		ModelAndView modelAndView = new ModelAndView("cadastro/cadastroPessoa");
		modelAndView.addObject("pessoas", pessoaRepository.findAll());
		modelAndView.addObject("pessoaObj", new Pessoa());
		return modelAndView;
	}
	
	@PostMapping("/pesquisarPessoa")
	public ModelAndView pesquisar(@RequestParam("nomePesquisa") String nomePesquisa,
								  @RequestParam("pesqSexo") String pesqSexo) {

		List<Pessoa> pessoas = new ArrayList<Pessoa>();

		if(pesqSexo != null && !pesqSexo.isEmpty()){
			pessoas = pessoaRepository.findPessoaByNameSexo(nomePesquisa, pesqSexo);
		} else {
			pessoas = pessoaRepository.findPessoaByName(nomePesquisa);
		}

		ModelAndView modelAndView = new ModelAndView("cadastro/cadastroPessoa");
		modelAndView.addObject("pessoas", pessoas);
		modelAndView.addObject("pessoaObj", new Pessoa());
		return modelAndView;
	}
	
	@GetMapping("/telefones/{idPessoa}")
	public ModelAndView telefones(@PathVariable("idPessoa") Long idPessoa) {
		
		Optional<Pessoa> pessoa = pessoaRepository.findById(idPessoa);
		
		ModelAndView modelAndView = new ModelAndView("cadastro/telefones");
		modelAndView.addObject("pessoaObj", pessoa.get());
		modelAndView.addObject("telefones", telefoneRepository.getTelefones(idPessoa));
		return modelAndView;
	}
	
	@PostMapping("/addFonePessoa/{pessoaId}")
	public ModelAndView addFonePessoa(Telefone telefone, @PathVariable("pessoaId") Long pessoaId) {
		
		Pessoa pessoa = pessoaRepository.findById(pessoaId).get();
		
		if(telefone != null && (telefone.getNumero() != null && telefone.getNumero().isEmpty()) || telefone.getNumero() == null) {
			ModelAndView modelAndView = new ModelAndView("cadastro/telefones");
			modelAndView.addObject("pessoaObj", pessoa);
			modelAndView.addObject("telefones", telefoneRepository.getTelefones(pessoaId));
			List<String> msg = new ArrayList<String>();
			msg.add("Número deve ser informado");
			modelAndView.addObject("msg", msg);
			return modelAndView;
		}
		
		
		telefone.setPessoa(pessoa);
		telefoneRepository.save(telefone);
		
		ModelAndView modelAndView = new ModelAndView("cadastro/telefones");
		modelAndView.addObject("pessoaObj", pessoa);
		modelAndView.addObject("telefones", telefoneRepository.getTelefones(pessoaId));
		return modelAndView;
	}
	
	@GetMapping("/removerTelefone/{idTelefone}")
	public ModelAndView removerTelefone(@PathVariable("idTelefone") Long idTelefone) {
		
		Pessoa pessoa = telefoneRepository.findById(idTelefone).get().getPessoa();
		
		telefoneRepository.deleteById(idTelefone);
		
		ModelAndView modelAndView = new ModelAndView("cadastro/telefones");
		modelAndView.addObject("pessoaObj", pessoa);
		modelAndView.addObject("telefones", telefoneRepository.getTelefones(pessoa.getId()));
		return modelAndView;
	}

//	@GetMapping("/baixarCurriculo/{idPessoa}")
//	public void baixarCurriculo(@PathVariable("idPessoa") Long idPessoa, HttpServletResponse response) throws IOException {
//
//		Pessoa pessoa = pessoaRepository.findById(idPessoa).get();
//		if(pessoa.getCurriculo() != null){
//			response.setContentLength(pessoa.getCurriculo().length);
//			response.setContentType(pessoa.getTipoFileCurriculo());
//			String headerKey = "Content-Disposition";
//			String headerValue = String.format("attachment; filename=\"%s\"", pessoa.getNomeFileCurriculo());
//			response.setHeader(headerKey, headerValue);
//
//			response.getOutputStream().write(pessoa.getCurriculo());
//		}
//	}

	@GetMapping("/baixarCurriculo/{idPessoa}")
	public ResponseEntity<byte[]> baixarCurriculo(@PathVariable("idPessoa") Long idPessoa) throws IOException {
		Pessoa pessoa = pessoaRepository.findById(idPessoa).orElse(null);

		if (pessoa != null && pessoa.getCurriculo() != null) {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			headers.setContentDispositionFormData("attachment", pessoa.getNomeFileCurriculo());
			headers.setContentLength(pessoa.getCurriculo().length);

			return new ResponseEntity<>(pessoa.getCurriculo(), headers, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}


}
