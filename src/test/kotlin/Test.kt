import org.junit.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.Serializable
import interfaces.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.encodeToJsonElement

data class TestCase(val expected: String, val query: String)

fun ExpressionBinary(left: IExpression, tok: String, right: IExpression): IExpression
{
    return ExpressionProgram(left, mutableListOf(ExpressionOperation(tok, right)))
}

class Test
{
    @Test fun example()
    {
        val CASES = listOf(
            TestCase("""
                {
                   "stmt":{
                      "columns":[
                         {
                            "type":"interfaces.ColumnStar"
                         }
                      ],
                      "from":{
                         "type":"interfaces.FromTableOrSubqueryList",
                         "tableOrSubqueryList":[
                            {
                               "type":"interfaces.TableName",
                               "tableName":"book"
                            }
                         ]
                      }
                   }
                }
        """, "SELECT * FROM book;"),

            TestCase("""
                {
                   "stmt":{
                      "columns":[
                         {
                            "type":"interfaces.ColumnStar"
                         }
                      ],
                      "from":{
                         "type":"interfaces.FromTableOrSubqueryList",
                         "tableOrSubqueryList":[
                            {
                               "type":"interfaces.Subquery",
                               "stmt":{
                                  "type":"interfaces.SelectStmt",
                                  "columns":[
                                     {
                                        "type":"interfaces.ColumnStar"
                                     }
                                  ],
                                  "from":{
                                     "type":"interfaces.FromTableOrSubqueryList",
                                     "tableOrSubqueryList":[
                                        {
                                           "type":"interfaces.TableName",
                                           "tableName":"A"
                                        }
                                     ]
                                  }
                               },
                               "alias":"a_alias"
                            }
                         ]
                      }
                   }
                }
            """, "SELECT * FROM (SELECT * FROM A) a_alias;"),



            TestCase("""
                {
                   "stmt":{
                      "columns":[
                         {
                            "type":"interfaces.ColumnStar"
                         }
                      ],
                      "from":{
                         "type":"interfaces.FromTableOrSubqueryList",
                         "tableOrSubqueryList":[
                            {
                               "type":"interfaces.TableName",
                               "tableName":"a"
                            },
                            {
                               "type":"interfaces.TableName",
                               "tableName":"b"
                            },
                            {
                               "type":"interfaces.TableName",
                               "tableName":"c"
                            }
                         ]
                      }
                   }
                }
            """, "SELECT * FROM a,b,c;"),


            TestCase("""
                {
                   "stmt":{
                      "columns":[
                         {
                            "type":"interfaces.Column",
                            "expr":{
                               "type":"interfaces.ExpressionIdentifier",
                               "value":"marvel.id"
                            },
                            "alias":"id"
                         },
                         {
                            "type":"interfaces.Column",
                            "expr":{
                               "type":"interfaces.ExpressionIdentifier",
                               "value":"marvel.name"
                            },
                            "alias":"name"
                         },
                         {
                            "type":"interfaces.Column",
                            "expr":{
                               "type":"interfaces.ExpressionIdentifier",
                               "value":"marvel.abilities"
                            },
                            "alias":"abilities"
                         }
                      ],
                      "from":{
                         "type":"interfaces.FromTableOrSubqueryList",
                         "tableOrSubqueryList":[
                            {
                               "type":"interfaces.TableName",
                               "tableName":"marvel"
                            }
                         ]
                      }
                   }
                }
            """, "SELECT marvel.id AS id, marvel.name AS name, marvel.abilities AS abilities FROM marvel;"),


            TestCase("""
                {
                   "stmt":{
                      "columns":[
                         {
                            "type":"interfaces.ColumnStar"
                         }
                      ],
                      "from":{
                         "type":"interfaces.FromTableOrSubqueryList",
                         "tableOrSubqueryList":[
                            {
                               "type":"interfaces.TableName",
                               "tableName":"marvel"
                            }
                         ]
                      },
                      "whereClause":{
                         "type":"interfaces.ExpressionProgram",
                         "first":{
                            "type":"interfaces.ExpressionProgram",
                            "first":{
                               "type":"interfaces.ExpressionIdentifier",
                               "value":"id"
                            },
                            "rest":[
                               {
                                  "tok":">",
                                  "operand":{
                                     "type":"interfaces.ExpressionIntegralLiteral",
                                     "value":1
                                  }
                               }
                            ]
                         },
                         "rest":[
                            {
                               "tok":"AND",
                               "operand":{
                                  "type":"interfaces.ExpressionProgram",
                                  "first":{
                                     "type":"interfaces.ExpressionIdentifier",
                                     "value":"id"
                                  },
                                  "rest":[
                                     {
                                        "tok":"<=",
                                        "operand":{
                                           "type":"interfaces.ExpressionIntegralLiteral",
                                           "value":5
                                        }
                                     }
                                  ]
                               }
                            }
                         ]
                      }
                   }
                }
            """, "SELECT * FROM marvel WHERE id > 1 AND id <=5;"),


            TestCase("""
                {
                   "stmt":{
                      "columns":[
                         {
                            "type":"interfaces.ColumnStar"
                         }
                      ],
                      "from":{
                         "type":"interfaces.FromTableOrSubqueryList",
                         "tableOrSubqueryList":[
                            {
                               "type":"interfaces.TableName",
                               "tableName":"marvel"
                            }
                         ]
                      },
                      "limit":{
                         "type":"interfaces.ExpressionIntegralLiteral",
                         "value":3
                      }
                   }
                }
            """, "SELECT * FROM marvel LIMIT 3;"),


            TestCase("""
                {
                   "stmt":{
                      "columns":[
                         {
                            "type":"interfaces.ColumnStar"
                         }
                      ],
                      "from":{
                         "type":"interfaces.FromTableOrSubqueryList",
                         "tableOrSubqueryList":[
                            {
                               "type":"interfaces.TableName",
                               "tableName":"marvel"
                            }
                         ]
                      },
                      "limit":{
                         "type":"interfaces.ExpressionIntegralLiteral",
                         "value":3
                      },
                      "offset":{
                         "type":"interfaces.ExpressionIntegralLiteral",
                         "value":2
                      }
                   }
                }
            """, "SELECT * FROM marvel LIMIT 3 OFFSET 2;"),


            TestCase("""
                {
                   "stmt":{
                      "columns":[
                         {
                            "type":"interfaces.ColumnStar"
                         }
                      ],
                      "from":{
                         "type":"interfaces.FromTableOrSubqueryList",
                         "tableOrSubqueryList":[
                            {
                               "type":"interfaces.TableName",
                               "tableName":"marvel"
                            }
                         ]
                      },
                      "groupByColumns":[
                         {
                            "type":"interfaces.ExpressionIdentifier",
                            "value":"name"
                         }
                      ]
                   }
                }
            """, "SELECT * FROM marvel GROUP BY name;"),


            TestCase("""
                {
                   "stmt":{
                      "columns":[
                         {
                            "type":"interfaces.ColumnStar"
                         }
                      ],
                      "from":{
                         "type":"interfaces.FromTableOrSubqueryList",
                         "tableOrSubqueryList":[
                            {
                               "type":"interfaces.TableName",
                               "tableName":"marvel"
                            }
                         ]
                      },
                      "sortColumns":[
                         {
                            "type":"interfaces.ExpressionIdentifier",
                            "value":"name"
                         }
                      ]
                   }
                }
            """, "SELECT * from marvel ORDER BY name;"),


            TestCase("""
                {
                   "stmt":{
                      "columns":[
                         {
                            "type":"interfaces.ColumnStar"
                         }
                      ],
                      "from":{
                         "type":"interfaces.FromJoinClause",
                         "tableOrSubquery":{
                            "type":"interfaces.TableName",
                            "tableName":"objects"
                         },
                         "joinOp":"INNER",
                         "joinTableOrSubquery":{
                            "type":"interfaces.TableName",
                            "tableName":"users"
                         },
                         "joinConstraint":{
                            "type":"interfaces.ExpressionProgram",
                            "first":{
                               "type":"interfaces.ExpressionIdentifier",
                               "value":"users.id"
                            },
                            "rest":[
                               {
                                  "tok":"==",
                                  "operand":{
                                     "type":"interfaces.ExpressionIdentifier",
                                     "value":"objects.id"
                                  }
                               }
                            ]
                         }
                      }
                   }
                }
            """, "SELECT * FROM objects INNER JOIN users ON users.id == objects.id;"),


            TestCase("""
                {
                   "stmt":{
                      "columns":[
                         {
                            "type":"interfaces.ColumnStar"
                         }
                      ],
                      "from":{
                         "type":"interfaces.FromJoinClause",
                         "tableOrSubquery":{
                            "type":"interfaces.TableName",
                            "tableName":"objects"
                         },
                         "joinOp":"LEFT",
                         "joinTableOrSubquery":{
                            "type":"interfaces.TableName",
                            "tableName":"users"
                         },
                         "joinConstraint":{
                            "type":"interfaces.ExpressionProgram",
                            "first":{
                               "type":"interfaces.ExpressionIdentifier",
                               "value":"users.id"
                            },
                            "rest":[
                               {
                                  "tok":"==",
                                  "operand":{
                                     "type":"interfaces.ExpressionIdentifier",
                                     "value":"objects.id"
                                  }
                               }
                            ]
                         }
                      }
                   }
                }
            """, "SELECT * FROM objects LEFT JOIN users ON users.id == objects.id;"),


            TestCase("""
                {
                   "stmt":{
                      "columns":[
                         {
                            "type":"interfaces.ColumnStar"
                         }
                      ],
                      "from":{
                         "type":"interfaces.FromJoinClause",
                         "tableOrSubquery":{
                            "type":"interfaces.TableName",
                            "tableName":"objects"
                         },
                         "joinOp":"RIGHT",
                         "joinTableOrSubquery":{
                            "type":"interfaces.TableName",
                            "tableName":"users"
                         },
                         "joinConstraint":{
                            "type":"interfaces.ExpressionProgram",
                            "first":{
                               "type":"interfaces.ExpressionIdentifier",
                               "value":"users.id"
                            },
                            "rest":[
                               {
                                  "tok":"==",
                                  "operand":{
                                     "type":"interfaces.ExpressionIdentifier",
                                     "value":"objects.id"
                                  }
                               }
                            ]
                         }
                      }
                   }
                }
            """, "SELECT * FROM objects RIGHT JOIN users ON users.id == objects.id;"),


            TestCase("""
                {
                   "stmt":{
                      "columns":[
                         {
                            "type":"interfaces.ColumnStar"
                         }
                      ],
                      "from":{
                         "type":"interfaces.FromJoinClause",
                         "tableOrSubquery":{
                            "type":"interfaces.TableName",
                            "tableName":"objects"
                         },
                         "joinOp":"FULL",
                         "joinTableOrSubquery":{
                            "type":"interfaces.TableName",
                            "tableName":"users"
                         },
                         "joinConstraint":{
                            "type":"interfaces.ExpressionProgram",
                            "first":{
                               "type":"interfaces.ExpressionIdentifier",
                               "value":"users.id"
                            },
                            "rest":[
                               {
                                  "tok":"==",
                                  "operand":{
                                     "type":"interfaces.ExpressionIdentifier",
                                     "value":"objects.id"
                                  }
                               }
                            ]
                         }
                      }
                   }
                }
            """, "SELECT * FROM objects FULL JOIN users ON users.id == objects.id;"),


            TestCase("""
                {
                   "stmt":{
                      "columns":[
                         {
                            "type":"interfaces.Column",
                            "expr":{
                               "type":"interfaces.ExpressionIdentifier",
                               "value":"author.name"
                            }
                         },
                         {
                            "type":"interfaces.Column",
                            "expr":{
                               "type":"interfaces.ExpressionFunctionCall",
                               "value":"count",
                               "args":[
                                  {
                                     "type":"interfaces.ExpressionIdentifier",
                                     "value":"book.id"
                                  }
                               ]
                            }
                         },
                         {
                            "type":"interfaces.Column",
                            "expr":{
                               "type":"interfaces.ExpressionFunctionCall",
                               "value":"sum",
                               "args":[
                                  {
                                     "type":"interfaces.ExpressionIdentifier",
                                     "value":"book.cost"
                                  }
                               ]
                            }
                         }
                      ],
                      "from":{
                         "type":"interfaces.FromJoinClause",
                         "tableOrSubquery":{
                            "type":"interfaces.TableName",
                            "tableName":"author"
                         },
                         "joinOp":"LEFT",
                         "joinTableOrSubquery":{
                            "type":"interfaces.TableName",
                            "tableName":"book"
                         },
                         "joinConstraint":{
                            "type":"interfaces.ExpressionProgram",
                            "first":{
                               "type":"interfaces.ExpressionIdentifier",
                               "value":"author.id"
                            },
                            "rest":[
                               {
                                  "tok":"=",
                                  "operand":{
                                     "type":"interfaces.ExpressionIdentifier",
                                     "value":"book.author_id"
                                  }
                               }
                            ]
                         }
                      },
                      "groupByColumns":[
                         {
                            "type":"interfaces.ExpressionIdentifier",
                            "value":"author.name"
                         }
                      ],
                      "having":{
                         "type":"interfaces.ExpressionProgram",
                         "first":{
                            "type":"interfaces.ExpressionProgram",
                            "first":{
                               "type":"interfaces.ExpressionFunctionCallStar",
                               "value":"COUNT"
                            },
                            "rest":[
                               {
                                  "tok":">",
                                  "operand":{
                                     "type":"interfaces.ExpressionIntegralLiteral",
                                     "value":1
                                  }
                               }
                            ]
                         },
                         "rest":[
                            {
                               "tok":"AND",
                               "operand":{
                                  "type":"interfaces.ExpressionProgram",
                                  "first":{
                                     "type":"interfaces.ExpressionFunctionCall",
                                     "value":"SUM",
                                     "args":[
                                        {
                                           "type":"interfaces.ExpressionIdentifier",
                                           "value":"book.cost"
                                        }
                                     ]
                                  },
                                  "rest":[
                                     {
                                        "tok":">",
                                        "operand":{
                                           "type":"interfaces.ExpressionIntegralLiteral",
                                           "value":500
                                        }
                                     }
                                  ]
                               }
                            }
                         ]
                      },
                      "limit":{
                         "type":"interfaces.ExpressionIntegralLiteral",
                         "value":10
                      }
                   }
                }
            """,
            """
                SELECT author.name, count(book.id), sum(book.cost) 
                FROM author 
                LEFT JOIN book ON (author.id = book.author_id) 
                GROUP BY author.name HAVING COUNT(*) > 1 AND SUM(book.cost) > 500
                LIMIT 10;
            """)
        )
//


//        var q = Query()
//        q.parse("SELECT marvel.id AS id, marvel.name AS name, marvel.abilities AS abilities FROM marvel;")
//        println(Json.encodeToJsonElement(q))

        for (case in CASES)
        {
            var q = Query()
            q.parse(case.query)
            assertEquals(Json.decodeFromString(case.expected), Json.encodeToJsonElement(q))
        }
    }
}

