<?php

//update location of the user

require("config.inc.php");

if (!empty($_POST)) {

$timestamp = time();
$coord = $_POST['coordinator'];

$query = " SELECT 1 FROM coordinators WHERE leader = :user AND groupname = :groupID";
$query_params = array(
        ':user' => $_POST['username'],
        ':groupID' => $_POST['groupID']
    );
try { 
        $stmt   = $db->prepare($query);
        $result = $stmt->execute($query_params);
    }
    catch (PDOException $ex) {
        $response["success"] = 0;
        $response["message"] = "Coordinator table Error. Please Try Again!";
        die(json_encode($response));
    }

    $row = $stmt->fetch();
    if ($row) {                   
	   $coord =1; 
           $response["success"] = 1;
    $response["message"] = "LEADER REFRESHED!";
    $response["coord"] = $coord;
    echo json_encode($response);
    
    }

	//UPDATE POSITION
    $query        = " SELECT 1 FROM positions WHERE username = :user";
    $query_params = array(
        ':user' => $_POST['username']
    );

    try {
         
        $stmt   = $db->prepare($query);
        $result = $stmt->execute($query_params);
    }
    catch (PDOException $ex) {
        $response["success"] = 0;
        $response["message"] = "Database Error1. Please Try Again!";
        die(json_encode($response));
    }
    
    $row = $stmt->fetch();
    if ($row) {
                     
    $query = "UPDATE positions SET groupID=:group, latitude=:lat, longitude=:long, lastUP='$timestamp', coordinator='$coord' WHERE username=:user" ;
    
    $query_params = array(
        ':user' => $_POST['username'],
        ':group' => $_POST['groupID'],
		':lat' => $_POST['latitude'],
        ':long' => $_POST['longitude']
    );
    
    try {
        $stmt   = $db->prepare($query);
        $result = $stmt->execute($query_params);
    }
    catch (PDOException $ex) {

        $response["success"] = 0;
        $response["message"] = "Database Error. Couldn't UPDATE position!";
        die(json_encode($response));
    }

    $response["success"] = 1;
    $response["message"] = "Position Successfully Updated!";
    $response["coord"] = $coordinator;
    echo json_encode($response);
   
        
        
    }else{ 
    
	//INSERT RECORD

	$query = "INSERT INTO positions(username, groupID, latitude, longitude, lastUP, coordinator) VALUES (:user,:group,:lat,:long, '$timestamp', '$coord')" ;

    //Insert query
    $query_params = array(
        ':user' => $_POST['username'],
        ':group' => $_POST['groupID'],
		':lat' => $_POST['latitude'],
        ':long' => $_POST['longitude']
    );

    try {
        $stmt   = $db->prepare($query);
        $result = $stmt->execute($query_params);
        
    }
    catch (PDOException $ex) {
        $response["success"] = 0;
        $response["message"] = "Database Error. Couldn't INSERT position!";
        die(json_encode($response));
    }

    $response["success"] = 1;
    $response["message"] = "Position Successfully Updated!";
    echo json_encode($response);
    }
   
} 
?>
