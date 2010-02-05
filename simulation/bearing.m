function theta = bearing(tower_cart, mouse_cart)
    v = mouse_cart - tower_cart;
    theta = cart2pol(v(1), v(2));