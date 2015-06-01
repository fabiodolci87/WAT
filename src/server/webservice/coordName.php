<?php

//Used to find the name of the coordinator

require("config.inc.php");

$query = "Select * FROM coordinators WHERE groupname='serina'";

$query_params = array(
        ':gruppo' => $_POST['groupname']
        );

try {
    $stmt   = $db->prepare($query);
    $result = $stmt->execute($query_params);
}
catch (PDOException $ex) {
    $response["success"] = 0;
    $response["message"] = "Database Error!";
    die(json_encode($response));
}

$rows = $stmt->fetchAll();

if ($rows) {
    $response["success"] = 1;
    $response["message"] = "Post Available!";
    $response["posts"]   = array();
    
    foreach ($rows as $row) {
        $post["leader"] = $row["leader"];
        array_push($response["posts"], $post);
    }
    
    echo json_encode($response);
    
} else {
    $response["success"] = 0;
    $response["message"] = "No Post Available!";
    die(json_encode($response));
}
?>

