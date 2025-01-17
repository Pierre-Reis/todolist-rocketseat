package br.com.pierre.todolist.task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.pierre.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/tasks")
public class TaskController {
    
    @Autowired
    private ITaskRepository taskRepository;

    @GetMapping("/list")
    public List<TaskModel> getTasks(HttpServletRequest request) {
        
        var idUser = request.getAttribute("idUser");

        return this.taskRepository.findByIdUser((UUID)idUser);
    }
    
    @PostMapping("/create")
    @SuppressWarnings("rawtypes")
    public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest request){
        
        var idUser = request.getAttribute("idUser");
        taskModel.setIdUser((UUID)idUser);
        
        var currentDate = LocalDateTime.now();

        if(currentDate.isAfter(taskModel.getStartAt()) || currentDate.isAfter(taskModel.getEndAt())){
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("A data de início / término deve ser posterior a data atual.");
        }

        if (taskModel.getStartAt().isAfter(taskModel.getEndAt())){
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("A data de término não pode ser maior que a data de início.");
        }

        var task = this.taskRepository.save(taskModel);
        return ResponseEntity.status(HttpStatus.OK).body(task);
    }

    @PutMapping("/update/{id}")
    @SuppressWarnings("rawtypes")
    public ResponseEntity update(@RequestBody TaskModel taskModel, HttpServletRequest request, @PathVariable UUID id){

        var task = this.taskRepository.findById(id).orElse(null);

        if(task == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("A tarefa de id " + id + " não existe");
        }
                
        var idUser = request.getAttribute("idUser");

        if(!task.getIdUser().equals(idUser)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("O usuário não tem permissão para alterar essa tarefa");
        }

        Utils.copyNonNullProperties(taskModel, task);

        var taskUpdated = this.taskRepository.save(task);

        return ResponseEntity.ok().body(taskUpdated); 
    }
}