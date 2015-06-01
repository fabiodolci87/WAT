<?php
//Add a a group to the database or let the coordinator rejoin it

require("config.inc.php");

$timestamp = time();
$gruppo = $_POST["groupID"];
$coordinator=1;

//if posted data is not empty
if (!empty($_POST)) {
    
    if (empty($_POST['username']) || empty($_POST['groupID'])) {
         
        $response["success"] = 0;
        $response["message"] = "Please Enter Group name";
        
        die(json_encode($response));
    }
    
//check if the coordinator tries to login again and let it

$query        = " SELECT 1 FROM positions WHERE groupID = :groupID AND username = :username AND coordinator=1";
    
    $query_params = array(
        ':groupID' => $_POST['groupID'],
        ':username' => $_POST['username']
    );
   
    try {
        
        $stmt   = $db->prepare($query);
        $result = $stmt->execute($query_params);
    }
    catch (PDOException $ex) {
        
        $response["success"] = 0;
        $response["message"] = "ERROR DB";
        die(json_encode($response));
    }
    
   
    $row = $stmt->fetch();
    if ($row) {
        
        $response["success"] = 1;
        $response["message"] = "Welcome back leader of $gruppo";
        die(json_encode($response));
        goto a;
    }

// check if there is another group with the same name
    $query        = " SELECT 1 FROM positions WHERE groupID = :groupID AND coordinator=1";
    
    $query_params = array(
        ':groupID' => $_POST['groupID']
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
        
        $response["success"] = 0;
        $response["message"] = "I'm sorry, $gruppo is already in use";
        $response["gruppo"] = $gruppo;
        $response["coordinator"] = $coordinator;
        die(json_encode($response));
    }
    
    
    //ADD USERNAME TO POSITION IN ORDER TO FURTHER UPDATES
    
    $query = " SELECT 1 FROM positions WHERE username = :user";
    
    $query_params = array(
        ':user' => $_POST['username']
    );
    
    try {
         
        $stmt   = $db->prepare($query);
        $result = $stmt->execute($query_params);
    }
    catch (PDOException $ex) {

        $response["success"] = 0;
        $response["message"] = "Database Error. Please Try Again!";
        die(json_encode($response));
    }
    
    
    $row = $stmt->fetch();
    if ($row) {
     
     //if this user is on the table
     //ADD GROUP TO THE POSITIONS TABLE (UPDATING EXISTING USER)
    $query1 = "UPDATE positions SET groupID=:groupID, latitude='0', longitude='0', lastUP='$timestamp', coordinator='$coordinator' WHERE username=:user";
    
    $query_params1 = array(
        ':user' => $_POST['username'],
        ':groupID' => $_POST['groupID']
    );
    
   
    try {
        $stmt1   = $db->prepare($query1);
        $result1 = $stmt1->execute($query_params1);
    }
    catch (PDOException $ex) {
        $response["success"] = 0;
        $response["message"] = "Database Error2. Please Try Again!";
        die(json_encode($response));
    }
    
    $response["success"] = 1;
    $response["message"] = "Group Successfully Updated!";
    $response["coord"] = $coordinator;
    echo json_encode($response);
    
    //UPDATE LEADER
    $query1 = "UPDATE coordinators SET groupname=:groupID WHERE leader=:user";
    
    $query_params1 = array(
        ':user' => $_POST['username'],
        ':groupID' => $_POST['groupID']
    );
    
   
    try {
        $stmt1   = $db->prepare($query1);
        $result1 = $stmt1->execute($query_params1);
    }
    catch (PDOException $ex) {
        $response["success"] = 0;
        $response["message"] = "Database Error2. Please Try Again!";
        die(json_encode($response));
    }
    
    $response["success"] = 1;
    $response["message"] = "Leader Successfully Added!";
    $response["coord"] = $coordinator;
    echo json_encode($response);
    
    //
    }
    
    // IF NOT PRESENT, ADD USER TO POSITIONS AND CREATE THE GROUP
    
    else {
    $query = "INSERT INTO positions(username, groupID, latitude, longitude, lastUP, coordinator) VALUES (:user,:group,'0','0', '$timestamp', '$coordinator')" ;

    $query_params = array(
        ':user' => $_POST['username'],
        ':group' => $_POST['groupID']
    );
  
    try {
        $stmt   = $db->prepare($query);
        $result = $stmt->execute($query_params);
                
    }
    catch (PDOException $ex) {

        $response["success"] = 0;
        $response["message"] = "Database Error. Couldn't INSERT group!";
        die(json_encode($response));
    }
    
    //ADD LEADER TO THE COORDINATORS TABLE
    $query_leader = "INSERT INTO coordinators (leader, groupname) VALUES (:user,:group)" ;

    $query_params = array(
        ':user' => $_POST['username'],
        ':group' => $_POST['groupID']
    );
  
    try {
        $stmt_leader   = $db->prepare($query_leader);
        $result = $stmt_leader->execute($query_params);
        
    }
    catch (PDOException $ex) {

        $response["success"] = 0;
        $response["message"] = "Database Error. Couldn't INSERT leader!";
        die(json_encode($response));
    }
    
    $response["success"] = 1;
    $response["message"] = "Leader Successfully Added!";
    $response["coord"] = $coordinator;
    echo json_encode($response);
    }
    
a:        
    
} 

?>
