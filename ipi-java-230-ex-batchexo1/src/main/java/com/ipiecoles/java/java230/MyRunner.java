package com.ipiecoles.java.java230;

import com.ipiecoles.java.java230.exceptions.BatchException;
import com.ipiecoles.java.java230.exceptions.TechnicienException;
import com.ipiecoles.java.java230.model.Commercial;
import com.ipiecoles.java.java230.model.Employe;
import com.ipiecoles.java.java230.model.Manager;
import com.ipiecoles.java.java230.model.Technicien;
import com.ipiecoles.java.java230.repository.EmployeRepository;
import com.ipiecoles.java.java230.repository.ManagerRepository;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class MyRunner implements CommandLineRunner {

    private static final String REGEX_MATRICULE = "^[MTC][0-9]{5}$";
    private static final String REGEX_NOM = ".*";
    private static final String REGEX_PRENOM = ".*";
    private static final int NB_CHAMPS_MANAGER = 5;
    private static final int NB_CHAMPS_TECHNICIEN = 7;
    private static final int NB_CHAMPS_COMMERCIAL = 7;
    private static final String REGEX_MATRICULE_MANAGER = "^M[0-9]{5}$";


    @Autowired
    private EmployeRepository employeRepository;

    @Autowired
    private ManagerRepository managerRepository;

    private List<Employe> employes = new ArrayList<Employe>();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void run(String... strings) {
        String fileName = "employes.csv";
        try {
			readFile(fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
        //readFile(strings[0]);
    }

    /**
     * Méthode qui lit le fichier CSV en paramètre afin d'intégrer son contenu en BDD
     * @param fileName Le nom du fichier (à mettre dans src/main/resources)
     * @return une liste contenant les employés à insérer en BDD ou null si le fichier n'a pas pu être lu
     * @throws Exception  
     */
    public List<Employe> readFile(String fileName) throws Exception {
        Stream<String> stream;
        stream = Files.lines(Paths.get(new ClassPathResource(fileName).getURI()));
        Integer i=0;
        for (String ligne : stream.collect(Collectors.toList())) {
        	i++;
      
        	try {
        		processLine(ligne);
        		
        	} catch (BatchException e) {
        		System.out.println("Ligne " + i + " :" + e.getMessage() + " => " + ligne);
        	}
        }
        
        // On enregistre les employés dans la base
        // employeRepository.save(employes);
        
        return employes;
        
    }

    /**
     * Méthode qui regarde le premier caractère de la ligne et appelle la bonne méthode de création d'employé
     * @param ligne la ligne à analyser
     * @param regex 
     * @throws Exception 
     */
    
    private void processLine(String ligne) throws BatchException {
    	
    	switch (ligne.substring(0,1)){
        case "C" :
            processCommercial(ligne);
            break;
        case "M" :
            processManager(ligne);
            break;
        case "T" :
            processTechnicien(ligne);
            break;
        default:
            throw new BatchException("Type d'employé inconnu : " + ligne.substring(0,1));
    	}

    }
    

    /**
     * Méthode qui crée un Commercial à partir d'une ligne contenant les informations d'un commercial
     * et l'ajoute dans la liste globale des employés.
     * @param ligneCommercial la ligne contenant les infos du commercial à intégrer
     * @throws Exception 
     */
    
    private void processCommercial(String ligneCommercial) throws BatchException {
    	String[] comLine = ligneCommercial.split(",");
    	
    	// On vérifie qu'on a le bon nombre de champs renseignés pour un commercial et, le cas échéant, on créé une instance de commercial
    	
    	if (comLine.length != NB_CHAMPS_COMMERCIAL){
            throw new BatchException("La ligne manager ne contient pas " + NB_CHAMPS_COMMERCIAL + " éléments mais "
            + comLine.length + "  => " + ligneCommercial);
        }
    	
    	Commercial c = new Commercial();
    	
    	// On vérifie les champs communs à tous les employés et on affecte les valeurs pour chaque attribut
    	
    	checkCommonFields(comLine, c);
    	
    	// On vérifie les champs spécifiques aux commerciaux : CA et performance
    	
    	try {
    		Double.parseDouble(comLine[5]);
    	} catch (Exception e) {
    		throw new BatchException("Le chiffre d'affaire du commercial est incorrect : " + comLine[5]);
    		}
    	
    	try {
    		int performance = Integer.parseInt(comLine[6]);
    		if (performance < 0 || performance > 100) {
    			throw new BatchException("La performance du commercial est incorrecte :" + comLine[6]);
    		}
    	} catch (Exception e) {
    		throw new BatchException("La performance du commercial est incorrecte :" + comLine[6]);	
    		}
    	 	
    	// On affecte les valeurs des attributs spécifiques aux commerciaux
    	
    	c.setPerformance(Integer.parseInt(comLine[6]));
        c.setCaAnnuel(Double.parseDouble(comLine[5]));
        
        employes.add(c);
    	
    }
    
    /**
     * Méthode qui crée un Manager à partir d'une ligne contenant les informations d'un manager 
     * et l'ajoute dans la liste globale des employés
     * @param ligneManager la ligne contenant les infos du manager à intégrer
     * @throws BatchException s'il y a un problème sur cette ligne
     */
    
    private void processManager(String ligneManager) throws BatchException {
    	
    	String[] mgrLine = ligneManager.split(",");
    	
    	// On vérifie qu'on a le bon nombre de champs renseignés pour un manager et, le cas échéant, on créé une instance de manager
    	
    	if (mgrLine.length != NB_CHAMPS_MANAGER){
            throw new BatchException("La ligne manager ne contient pas " + NB_CHAMPS_MANAGER + " éléments mais " + mgrLine.length + " ");
        }
    	
    	Manager m = new Manager();
    	
    	// On vérifie les champs communs à tous les employés et on affecte les valeurs pour chaque attribut
    	
    	checkCommonFields(mgrLine, m);
    	      
        employes.add(m);
    }
    	
   

    /**
     * Méthode qui crée un Technicien à partir d'une ligne contenant les informations d'un technicien
     * et l'ajoute dans la liste globale des employés
     * @param ligneTechnicien la ligne contenant les infos du technicien à intégrer
     * @throws BatchException s'il y a un problème sur cette ligne
     * */
    private void processTechnicien(String ligneTechnicien) throws BatchException {
    	
    	String[] techLine = ligneTechnicien.split(",");
    	
    	// On vérifie qu'on a le bon nombre de champs renseignés pour un technicien et, le cas échéant, on créé une instance de technicien
    	
    	if (techLine.length != NB_CHAMPS_TECHNICIEN){
            throw new BatchException("La ligne technicien ne contient pas " + NB_CHAMPS_TECHNICIEN + " éléments mais " + techLine.length + "  ");
        }
    	
    	Technicien t = new Technicien();
    	  	
    	// On vérifie et on affecte la valeur du grade du technicien
    	
    	Integer grade;
    	
    	try {
        	grade = Integer.parseInt(techLine[5]);
    	} catch (Exception e) {
    		throw new BatchException("Le grade du technicien est incorrect " + techLine[5]);
    	}
    	
       	if (grade <= 0 || grade > 5) {
                throw new BatchException("Le grade doit être compris entre 1 et 5 : " + techLine[5]);
       	} else {
       		try {
				t.setGrade(grade);
			} catch (TechnicienException e) {
				e.printStackTrace();
			}
       	}        
        

		// On vérifie le matricule du manager du technicien et on l'affecte s'il est au bon format
	        if (!techLine[6].matches(REGEX_MATRICULE_MANAGER)) {
	            throw new BatchException("la chaîne " + techLine[6] + " ne respecte pas l'expression régulière : " + REGEX_MATRICULE_MANAGER);
	        }
	
	        employes.stream().forEach(emp -> {
	            if (emp instanceof Manager && emp.getMatricule() == techLine[6]) {
	                t.setManager((Manager) emp);
	            }
	        });
		        
		// On lance une exception si le matricule du manager n'existe pas dans la base de données
	        if (t.getManager() == null && employeRepository.findByMatricule(techLine[6]) == null) {
	            throw new BatchException("Le manager de matricule " + techLine[6] + " n'a pas été trouvé dans le fichier ou en base de données");
	        }
	        
		// On vérifie les champs communs à tous les employés et on affecte les valeurs pour chaque attribut
    	
	    	checkCommonFields(techLine, t);
	        		        			        
	        employes.add(t);
        
    }
    	   
    
    /**
     * Méthode qui vérifie les champs communs à tous les types d'employés.
     * @param tab le tableau contenant les infos de l'employé à intégrer
     * @throws BatchException s'il y a un problème sur cette ligne
     */
    
    private void checkCommonFields(String[] fields, Employe emp) throws BatchException {
    	
    	// On contrôle le matricule
        if (!fields[0].matches(REGEX_MATRICULE)) {
            throw new BatchException("la chaîne : " + fields[0] + " ne respecte pas l'expression régulière : " + REGEX_MATRICULE);
        }
        
    	// On contrôle le nom et prénom
    	if (!fields[1].matches(REGEX_NOM)){
            throw new BatchException(fields[1] + " n'est pas un nom valide ");
        }
        if (!fields[2].matches(REGEX_PRENOM)){
            throw new BatchException(fields[2] + " n'est pas un prénom valide ");
        }	
    		    	
    	// On contrôle le format de la date d'embauche, et, s'il est correct, on affecte la valeur du champ date d'embauche
        LocalDate date;
        try {
            date = DateTimeFormat.forPattern("dd/MM/yyyy").parseLocalDate(fields[3]);
            emp.setDateEmbauche(date);
        } catch (Exception e) {
            throw new BatchException(fields[3] + " ne respecte pas le format de date dd/MM/yyyy");
        }
        
        
    	// On contrôle le format du salaire et, s'il est correct, on affecte la valeur du champ salaire
        Double salaire;
        try {
            salaire = Double.parseDouble(fields[4]);
            
        } catch (Exception e) {
            throw new BatchException(fields[4] + " n'est pas un nombre valide pour un salaire");
        }
        
        // On affecte les valeurs des champs matricule, nom, prénom et salaire
        emp.setMatricule(fields[0]);
        emp.setNom(fields[1]);
        emp.setPrenom(fields[2]);
        emp.setSalaire(salaire);
                
	}

  
    
    
}