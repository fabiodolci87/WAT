<?php

//Used to retrieve all the comments

require("config.inc.php");

$query = "Select * FROM comments WHERE groupID=:gruppo ORDER by PostTime DESC";

$query_params = array(':gruppo' => $_POST['groupID']);

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
		$post["post_id"]  = $row["post_id"];
        $post             = array();
        $post["username"] = $row["username"];
        $post["title"]    = $row["title"];
        $post["message"]  = $row["message"];
        $post["name_pic"] = $row ["name_pic"];
        
        array_push($response["posts"], $post);
    }
    
    echo json_encode($response);

} else {
    $response["success"] = 0;
    $response["message"] = "No Post Available!";
    die(json_encode($response));
}
?>

