package com.scheduler;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "day_schedules")
public class DaySchedule {


	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "user_id", nullable = false)
    private Long userId;
	
	@Column(name = "category_name", nullable = false)
	private String categoryName;
	
	@Column(name = "category_color", nullable = false)
	private String categoryColor;
	
	@Column(nullable = false)
	private Integer weekday; // 0:月...6:日
	
   
	@Column(name = "start_time", nullable = false)
	private Integer startTime; // 0-1439
	
	@Column(name = "end_time", nullable = false)
	private Integer endTime; // 0-1439
	
	
	
    	//getter
    public Long getId(){ 
    	return id; 
    }
    
    public Long getUserId(){ 
    	return userId;
    }
    
    public String getCategoryName(){ 
    	return categoryName;
    }
    
    public String getCategoryColor(){ 
    	return categoryColor;
    }
    
    public Integer getWeekday(){
    	return weekday; 
    }
    
    public Integer getStartTime(){ 
    	return startTime;
    }
    
    public Integer getEndTime(){ 
    	return endTime; 
    }


    	//setter
    public void setId(Long id){
    	this.id = id; 
    }
    
    public void setUserId(Long userId){ 
    	this.userId = userId;
    }
    
    public void setCategoryName(String categoryName){ 
    	this.categoryName = categoryName;
    }
    
    public void setCategoryColor(String categoryColor){ 
    	this.categoryColor = categoryColor;
    }
    
    public void setWeekday(Integer weekday){ 
    	this.weekday = weekday;
    }
    
    public void setStartTime(Integer startTime){ 
    	this.startTime = startTime;
    }
    
    public void setEndTime(Integer endTime){ 
    	this.endTime = endTime; 
    }
    
}
