$(document).on("click", ".nav-item", function(){
    $(".nav-item.active").removeClass("active");
    $(this).addClass("active");
});