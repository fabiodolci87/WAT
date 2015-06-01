<?php

//used to delete itinerary and comments

require("config.inc.php");

//if posted data is not empty
if (!empty($_POST)) {
    
    if (empty($_POST['groupID'])) {
         
        $response["success"] = 0;
        $response["message"] = "Please Enter Group name";
        
        die(json_encode($response));
    }
     
   //delete 
    $query = " DELETE FROM `comments` WHERE  groupID = :groupID";
    
    $query_params = array(
        ':groupID' => $_POST['groupID']
    );
   
    try {
        
        $stmt   = $db->prepare($query);
        $result = $stmt->execute($query_params);
    }
    catch (PDOException $ex) {
        
        $response["success"] = 0;
        $response["message"] = "ERROR DB DELETING COMMENTS";
        die(json_encode($response));
    }
    
       //delete itinerary
    $query = " DELETE FROM `itineraries` WHERE  groupname = :groupID";
    
    $query_params = array(
        ':groupID' => $_POST['groupID']
    );
   
    try {
        
        $stmt   = $db->prepare($query);
        $result = $stmt->execute($query_params);
    }
    catch (PDOException $ex) {
        
        $response["success"] = 0;
        $response["message"] = "ERROR DB DELETING ITINERARY";
        die(json_encode($response));
    }

       //delete coordinator calls
    $query = " DELETE FROM `coord_calls` WHERE  groupID = :groupID";
    
    $query_params = array(
        ':groupID' => $_POST['groupID']
    );
   
    try {
        
        $stmt   = $db->prepare($query);
        $result = $stmt->execute($query_params);
    }
    catch (PDOException $ex) {
        
        $response["success"] = 0;
        $response["message"] = "ERROR DB DELETING CALLS";
        die(json_encode($response));
    }
    $response["success"] = 1;
        $response["message"] = "Deleted items";
        die(json_encode($response));
        }
?>
