<?php

//check if new comments are available for the group

require("config.inc.php");

$query = "SELECT * FROM comments WHERE groupID=:gruppo AND PostTime >= NOW() - INTERVAL 1 MINUTE";

$query_params = array(
        ':gruppo' => $_POST['groupID']
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
    die(json_encode($response));
} else {
    $response["success"] = 0;
    $response["message"] = "No Post Available!";
    die(json_encode($response));
}