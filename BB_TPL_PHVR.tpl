// Hybrid System Example: Bouncing Ball

g:=1; // constant acceleration from gravity

automaton bouncing_ball
   contr_var: x, v; //the position and velocity
   synclabs: jump;
   loc falling: while 0<=x & x<=10 & -10<=v & v<=10 wait {x'==v & v'==-g}
                when x==0 & v<0 sync jump do {v'==-v*%s & x'==x} goto falling;
   initially: falling & x==2 & v==0;
end

// --------------------- Analysis  begin-------------------------------------
bouncing_ball.add_label(tau);
pc:=%s;  //define partition constraint
bouncing_ball.set_partition_constraints((x,pc),(v,pc),tau); 

reg=bouncing_ball.reachable;  // reached part of state space
reg.print("out_bball",2);     // output as a list of vertices

// add commands here to check reachable states//
cond_v_excd_105 = bouncing_ball.{falling & v>=%s};
check_cond_v = bouncing_ball.is_reachable(cond_v_excd_105);

