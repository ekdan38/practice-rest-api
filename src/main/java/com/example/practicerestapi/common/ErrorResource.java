package com.example.practicerestapi.common;

import com.example.practicerestapi.index.IndexController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.validation.Errors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class ErrorResource extends EntityModel<Errors> {
    protected ErrorResource(Errors errors, Iterable<Link> links) {
        super(errors, links);
        add(linkTo(methodOn(IndexController.class).index()).withRel("index"));
    }
    public ErrorResource(Errors errors){
        super(errors, Links.NONE);
        add(linkTo(methodOn(IndexController.class).index()).withRel("index"));
    }
}
