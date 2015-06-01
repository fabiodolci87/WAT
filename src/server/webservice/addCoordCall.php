<?php

//Used to add a call from the coordinator to all the user

require("config.inc.php");

$query = "INSERT INTO coord_calls (groupID, longitude, latitude)
    			VALUES (:groupID,:longitude,:latitude)
                ON DUPLICATE KEY UPDATE longitude=:longitude, latitude=:latitude, time=NOW()";
    
$query_params = array(
    	':groupID'=> $_POST['groupID'],
        ':longitude' => $_POST['longitude'],
        ':latitude' => $_POST['latitude']
);
  
try {
        $stmt   = $db->prepare($query);
        $result = $stmt->execute($query_params);
    }
    
    catch (PDOException $ex) {
        $response["success"] = 0;
        $response["message"] = "Database Error. Couldn't add post!";
        die(json_encode($response));
    }
    $response["success"] = 1;
    $response["message"] = "Call Successfully Added!";
    $response["gruppo"] = $_POST['groupID'];
    echo json_encode($response);
?> 